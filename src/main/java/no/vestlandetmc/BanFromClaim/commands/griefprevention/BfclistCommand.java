package no.vestlandetmc.BanFromClaim.commands.griefprevention;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class BfclistCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof final Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final Location loc = player.getLocation();
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

		if(claim == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final String accessDenied = claim.allowGrantPermission(player);
		boolean allowBan = accessDenied == null;

		if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		}

		List<String> list = listPlayers(claim.getID().toString());

		if(list == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.LIST_EMPTY, null, player.getDisplayName(), claim.getOwnerName()));
			return true;
		}

		int size = list.size();

		int totalPage = ((size - 1) / 5) + 1;

		int countTo = Math.min(5, size) - 1;
		int countFrom = 0;
		int number = 1;

		if(args.length != 0) {
			if(isInt(args[0])) {
				number = Integer.parseInt(args[0]);
				if(number <= totalPage && number > 0){
					countTo = Math.min(5 * number, size) - 1;
					countFrom = 5 * number - 5;
				}else{
					number = 1;
				}
			}
			else {
				MessageHandler.sendMessage(player, Messages.UNVALID_NUMBER);
				return true;
			}
		}

		MessageHandler.sendMessage(player, Messages.placeholders(Messages.LIST_HEADER, null, player.getDisplayName(), claim.getOwnerName()));
		for(int i = countFrom; i <= countTo; i++) {
			final String bp = (String) list.toArray()[i];
			final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
			MessageHandler.sendMessage(player, "&6" + bannedPlayer.getName());
		}
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendMessage(player, "&e<--- [&6" + number + "\\" + totalPage + "&e] --->");

		return true;
	}

	private List<String> listPlayers(String claimID) {
		final ClaimData claimData = new ClaimData();

		return claimData.bannedPlayers(claimID);
	}

	private boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

}
