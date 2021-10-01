package no.vestlandetmc.BanFromClaim.listener;

import me.ryanhamshire.GPFlags.event.PlayerClaimBorderEvent;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Particles;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BfcListener implements Listener {

	@EventHandler
	public void onPlayerEnterClaim(PlayerClaimBorderEvent e) {

		final Player player = e.getPlayer();
		final Claim claim = e.getClaimTo();

		if(player.hasPermission("bfc.bypass")) { return; }

		if(claim != null) {
			final String claimID = claim.getID().toString();
			if(playerBanned(player, claimID)) {
				if(!MessageHandler.joinignore.contains(player.getUniqueId().toString())) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi spawn " + player.getName());
					MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
				}
			}
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent p) {

		final Player player = p.getPlayer();
		if(player.hasPermission("bfc.bypass")) return;

		MessageHandler.joinignore.add(player.getUniqueId().toString());
		new BukkitRunnable(){
			@Override
			public void run(){
				final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);

				if(claim != null) {
					final String claimID = claim.getID().toString();
					if(playerBanned(player, claimID)) {
						Particles.wall(player.getLocation());

						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi spawn " + player.getName());
						MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
						new BukkitRunnable(){
							@Override
							public void run(){
								MessageHandler.joinignore.remove(player.getUniqueId().toString());
							}
						}.runTaskLater(BfcPlugin.getInstance(), 1L);
					}
				}
			}
		}.runTaskLater(BfcPlugin.getInstance(), 22L);
	}

	private boolean playerBanned(Player player, String claimID) {
		final ClaimData claimData = new ClaimData();
		if(claimData.checkClaim(claimID)) {
			if(claimData.bannedPlayers(claimID) != null) {
				for(final String bp : claimData.bannedPlayers(claimID)) {
					if(bp.equals(player.getUniqueId().toString())) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
