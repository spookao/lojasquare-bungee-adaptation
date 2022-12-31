package br.com.lojasquare.bungee.event;

import br.com.lojasquare.bungee.ItemInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

@Getter
@RequiredArgsConstructor
public class ProductPreActiveEvent extends Event implements Cancellable {

    private final ProxiedPlayer proxiedPlayer;
    private final ItemInfo itemInfo;
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
