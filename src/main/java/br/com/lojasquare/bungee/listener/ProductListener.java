package br.com.lojasquare.bungee.listener;

import br.com.lojasquare.bungee.BungeeMain;
import br.com.lojasquare.bungee.ItemInfo;
import br.com.lojasquare.bungee.LojaSquare;
import br.com.lojasquare.bungee.event.ProductActiveEvent;
import br.com.lojasquare.bungee.event.ProductPreActiveEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProductListener implements Listener {

    @EventHandler
    public void preActive(final ProductPreActiveEvent e) {
        BungeeMain.printDebug("§3[LojaSquare] §bpreActive");
        if (!e.isCancelled()) {
            ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getInstance(), () -> {
                BungeeMain.printDebug("§3[LojaSquare] §bAntes update delivery.");
                final ItemInfo ii = e.getItemInfo();
                if (BungeeMain.getInstance().getLojaSquare().updateDelivery(ii)) {
                    BungeeMain.getInstance().print("§6[LojaSquare] §ePreparando entrega do produto do compra: §7" + ii.toString());
                    ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getInstance(), () -> {
                        ProductActiveEvent pae = new ProductActiveEvent(e.getProxiedPlayer(), ii);
                        ProxyServer.getInstance().getPluginManager().callEvent(pae);
                    });
                } else {
                    BungeeMain.printDebug("§4[LojaSquare] §cNao foi possivel atualizar o status da compra: §a" + ii.toString() + "§c para: 'Entregue'. Portanto, a entrega nao foi feita!");
                }
            });
        }
    }

    public String replaceCommand(ItemInfo ii, String cmds, int qntMoneyInteiro, double qntMoney) {
        cmds = cmds.replace("@moneyInteiro", "" + (qntMoneyInteiro > 0 ? qntMoneyInteiro : "")).replace("@money", qntMoney > 0.0D ? "" + qntMoney : "").replace("@grupo", ii.getGrupo());
        cmds = cmds.replace("@dias", String.valueOf(ii.getDias())).replace("@player", ii.getPlayer());
        cmds = cmds.replace("@qnt", String.valueOf(ii.getQuantidade())).replace("@cupom", ii.getCupom());
        return cmds;
    }

    @EventHandler
    public void activeSmartDelivery(ProductActiveEvent e) {
        ItemInfo ii = e.getItemInfo();
        if (e.isCancelled()) {
            BungeeMain.printDebug("§4[LojaSquare] §cAtivacao da compra: §a" + ii.toString() + "§c foi cancelada por meio do evento §aProductActiveEvent§c" + ", mas ja foi marcado no site com status 'Entregue'.");
        } else {
            if (BungeeMain.getInstance().doSmartDelivery()) {
                boolean isMoney = BungeeMain.getInstance().getConfiguration().getBoolean("Grupos." + ii.getGrupo() + ".Money");
                double qntMoney = 0.0D;
                if (isMoney) {
                    qntMoney = BungeeMain.getInstance().getConfiguration().getDouble("Grupos." + ii.getGrupo() + ".Quantidade_De_Money") * (double)ii.getQuantidade();
                }

                int qntMoneyInteiro = (int) qntMoney;
                for (String cmds : BungeeMain.getInstance().getConfiguration().getStringList("Grupos." + ii.getGrupo() + ".Cmds_A_Executar")) {
                    try {
                        cmds = this.replaceCommand(ii, cmds, qntMoneyInteiro, (double)qntMoneyInteiro);
                        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmds);
                    } catch (Exception var10) {
                        BungeeMain.getInstance().print("§4[LojaSquare] §cErro ao executar o cmd §a" + cmds + "§c da entrega com ID: §a" + ii.getIDEntrega() + "§c e codigo de transacao: §a" + ii.getCodigo() + "§c. Erro: §a" + var10.getMessage());
                        if (BungeeMain.getInstance().canDebug()) {
                            var10.printStackTrace();
                        }
                    }
                }

                BungeeMain.getInstance().print("§3[LojaSquare] §bEntrega do produto §a" + ii.toString() + "§b concluida com sucesso!");
                BungeeMain.printDebug("");
            }

        }
    }

    @EventHandler
    public void activeNoSmartDelivery(ProductActiveEvent e) {
        ItemInfo ii = e.getItemInfo();
        if (e.isCancelled()) {
            BungeeMain.getInstance().print("§4[LojaSquare] §cAtivacao da compra: §a" + ii.toString() + "§c foi cancelada por meio do evento §aProductActiveEvent§c" + ", mas ja foi marcado no site com status 'Entregue'.");
        } else {
            if (!BungeeMain.getInstance().doSmartDelivery()) {
                boolean isMoney = BungeeMain.getInstance().getConfiguration().getBoolean("Grupos." + ii.getGrupo() + ".Money");
                double qntMoney = 0.0D;
                if (isMoney) {
                    qntMoney = BungeeMain.getInstance().getConfiguration().getDouble("Grupos." + ii.getGrupo() + ".Quantidade_De_Money");
                }

                int qntMoneyInteiro = (int)qntMoney;

                for(int i = 1; i <= ii.getQuantidade(); ++i) {
                    for (String cmds : BungeeMain.getInstance().getConfiguration().getStringList("Grupos." + ii.getGrupo() + ".Cmds_A_Executar")) {
                        try {
                            cmds = this.replaceCommand(ii, cmds, qntMoneyInteiro, (double)qntMoneyInteiro);
                            ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmds);
                        } catch (Exception var11) {
                            BungeeMain.getInstance().print("§4[LojaSquare] §cErro ao executar o cmd §a" + cmds + "§c da entrega com ID: §a" + ii.getIDEntrega() + "§c e codigo de transacao: §a" + ii.getCodigo() + "§c. Erro: §a" + var11.getMessage());
                            if (BungeeMain.getInstance().canDebug()) {
                                var11.printStackTrace();
                            }
                        }
                    }

                    BungeeMain.getInstance().print("§3[LojaSquare] §bEntrega do produto §a" + ii.toString() + "§b concluida com sucesso!");
                    BungeeMain.printDebug("");
                }
            }

        }
    }

    @EventHandler
    public void sendMsgToPlayerOnActiveProducts(ProductActiveEvent e) {
        ItemInfo ii = e.getItemInfo();
        if (BungeeMain.getInstance().getConfiguration().getBoolean("Grupos." + ii.getGrupo() + ".Enviar_Mensagem", false)) {
            ProxiedPlayer p = e.getProxiedPlayer();
            for (String s : BungeeMain.getInstance().getConfiguration().getStringList("Grupos." + ii.getGrupo() + ".Mensagem_Receber_Ao_Ativar_Produto")) {
                s = s.replace("&", "§");
                s = s.replace("@grupo", ii.getGrupo()).replace("@produto", ii.getProduto()).replace("@dias", String.valueOf(ii.getDias()));
                s = s.replace("@qnt", String.valueOf(ii.getQuantidade())).replace("@player", p.getName()).replace("@cupom", ii.getCupom());
                p.sendMessage(s);
            }
        }
    }
}
