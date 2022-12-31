package br.com.lojasquare.bungee;

import br.com.lojasquare.bungee.event.ProductPreActiveEvent;
import br.com.lojasquare.bungee.listener.ProductListener;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BungeeMain extends Plugin {

    /**
     * Loja Square Adaptation to BungeeCord
     *
     * @author spookão
     */

    @Getter
    private static BungeeMain instance;
    private static int tempoChecarItens;
    private static List<String> produtosConfigurados = new ArrayList<>();
    private static LojaSquare ls;
    private static String servidor;
    private static boolean debug;
    private static boolean smartDelivery;
    @Getter
    private Configuration configuration;

    public static void printDebug(String s) {
        if (debug) {
            getInstance().getProxy().getConsole().sendMessage(s);
            for (ProxiedPlayer player : getInstance().getProxy().getPlayers()) {
                if (player.getGroups().contains("admin") || player.hasPermission("lojasquare.debug")) {
                    player.sendMessage(s);
                }
            }
        }
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = Files.newOutputStream(configFile.toPath())) {
                    ByteStreams.copy(is, os);
                }
            }
        } catch (Exception ignored) {

        }

        try {
            configuration = YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            getProxy().getConsole().sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");

            String keyapi = this.getKeyAPI();
            debug = getConfiguration().getBoolean("LojaSquare.Debug", true);
            servidor = getConfiguration().getString("LojaSquare.Servidor", null);
            if (!this.checarServidorConfigurado((ConsoleCommandSender) getProxy().getConsole())) {
                return;
            }

            smartDelivery = getConfiguration().getBoolean("LojaSquare.Smart_Delivery", true);
            getProxy().getConsole().sendMessage("§3[LojaSquare] §bAtivado...");
            getProxy().getConsole().sendMessage("§3Criador: §bTrow");
            getProxy().getConsole().sendMessage("§3Adaptado por: §bspookão");
            getProxy().getConsole().sendMessage("§bDesejo a voce uma otima experiencia com o §dLojaSquare§b.");
            getProxy().getConsole().sendMessage("§3[LojaSquare] §bIniciando o carregamento dos nomes dos grupos de itens para serem entregues...");

            for (String v : getConfiguration().getSection("Grupos").getKeys()) {
                produtosConfigurados.add(v);
                getProxy().getConsole().sendMessage("§3[LojaSquare] §bGrupo carregado: §a" + v);
            }

            getProxy().getConsole().sendMessage("§3[LojaSquare] §bGrupos de entregas foram carregados com sucesso!");
            tempoChecarItens = getConfiguration().getInt("Config.Tempo_Checar_Compras", 60);
            getProxy().getConsole().sendMessage("§3[LojaSquare] §bDefinindo variaveis de conexao com o site §dLojaSquare§b...");
            ls = new LojaSquare();
            ls.setCredencial(keyapi);
            ls.setConnectionTimeout(getConfiguration().getInt("LojaSquare.Connection_Timeout", 1500));
            ls.setReadTimeout(getConfiguration().getInt("LojaSquare.Read_Timeout", 3000));
            ls.setDebug(debug);
            getProxy().getConsole().sendMessage("§3[LojaSquare] §bVariaveis definidas com sucesso!");
            this.checarIPCorreto((ConsoleCommandSender) getProxy().getConsole(), keyapi);
            ProxyServer.getInstance().getPluginManager().registerListener(this, new ProductListener());
            this.checarEntregas((ConsoleCommandSender) getProxy().getConsole());
            getProxy().getConsole().sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        } catch (Exception var5) {
            var5.printStackTrace();
            getProxy().getConsole().sendMessage("§4[LojaSquare] §cErro ao iniciar o plugin LojaSquare.");
            getProxy().getConsole().sendMessage("§4[LojaSquare] §cErro: §a" + var5.getMessage());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public int getTempoChecarItens() {
        return Math.max(tempoChecarItens, 20);
    }

    public boolean doSmartDelivery() {
        return smartDelivery;
    }

    public LojaSquare getLojaSquare() {
        return ls;
    }

    public String getKeyAPI() {
        return this.getMsg("LojaSquare.Key_API");
    }

    public boolean produtoConfigurado(String grupo) {
        return produtosConfigurados.contains(grupo);
    }

    public String getMsg(String s) {
        if (getConfiguration().getString(s) == null) {
            getProxy().broadcast("§cLinha nao encontrada na config: §a" + s);
            return "";
        } else {
            return getConfiguration().getString(s).replace("&", "§");
        }
    }

    public void checarIPCorreto(final ConsoleCommandSender b, final String nome) {
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            String result = getLojaSquare().get("/v1/autenticar");
            if (result != null && result.contains("true")) {
                if (result.contains(",")) {
                    ls.setIpMaquina(result.split(",")[1]);
                }
                b.sendMessage("§3[LojaSquare] §bIP da maquina validado!");
            } else {
                b.sendMessage("§3[LojaSquare] §cDesativado...");
                b.sendMessage("§3Criador: §3Trow");
                b.sendMessage("§3Adaptado por: §3spookão");
                b.sendMessage("§cMotivo: " + result);
                b.sendMessage("§3Key-API: §a" + nome);
                b.sendMessage("§ePara atualizar o IP, acesse: §ahttps://painel.lojasquare.com.br/config/plugin");
                b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
            }
        });
    }

    public boolean checarServidorConfigurado(ConsoleCommandSender b) {
        if (servidor != null && !servidor.equalsIgnoreCase("Nome-Do-Servidor")) {
            return true;
        } else {
            b.sendMessage("§4[LojaSquare] §cDesativando...");
            b.sendMessage("§4[LojaSquare] §cPara que o plugin seja ativado com sucesso, e necessario configurar o nome do seu servidor na config.yml");
            b.sendMessage("§4[LojaSquare] §cAtualmente o nome do servidor esta definido como: §a" + (servidor == null ? "§4NAO DEFINIDO" : servidor));
            b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
            return false;
        }
    }

    public void checarEntregas(ConsoleCommandSender b) {
        b.sendMessage("§3[LojaSquare] §bIniciando checagem automatica de entregas...");
        b.sendMessage("§3[LojaSquare] §bTempo de checagem a cada §a" + this.getTempoChecarItens() + "§b segundos.");
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            List<ItemInfo> itens = getLojaSquare().getTodasEntregas();
            printDebug("");
            printDebug("§3[LojaSquare] §bItens Size: §a" + itens.size());
            if (itens.size() > 0) {
                Iterator<ItemInfo> iterator = itens.iterator();

                while (true) {
                    while (true) {
                        ItemInfo item;
                        ProxiedPlayer p;
                        boolean disputa;
                        boolean resolvido;
                        do {
                            do {
                                do {
                                    do {
                                        if (!iterator.hasNext()) {
                                            return;
                                        }

                                        item = iterator.next();
                                    } while (item == null);

                                    printDebug("§3[LojaSquare] §bItem: §a" + item.toString() + " §b// subServer: §a" + item.getSubServidor() + " // Servidor: §d" + servidor);
                                } while (!item.getSubServidor().equalsIgnoreCase(servidor));
                            } while (item.getStatusID() == 2);

                            p = ProxyServer.getInstance().getPlayer(item.getPlayer());
                            printDebug("§3[LojaSquare] §bPlayer: §a" + item.getPlayer() + "§b // P NULL? §a" + (p == null));
                            if (p != null || getConfiguration().getBoolean("Grupos." + item.getGrupo() + ".Ativar_Com_Player_Offline", false)) {
                                break;
                            }

                            disputa = item.getProduto().equalsIgnoreCase("DISPUTA") && item.getGrupo().equalsIgnoreCase("DISPUTA");
                            resolvido = item.getProduto().equalsIgnoreCase("RESOLVIDO") && item.getGrupo().equalsIgnoreCase("RESOLVIDO");
                        } while (!disputa && !resolvido);

                        if (!produtoConfigurado(item.getGrupo()) && p != null) {
                            printDebug("§3[LojaSquare] §bProduto §a" + item.getGrupo() + "§b nao configurado!");
                            p.sendMessage(getMsg("Msg.Produto_Nao_Configurado").replace("@grupo", item.getGrupo()));
                        } else {
                            printDebug("§3[LojaSquare] §bPre Product Active Event");
                            ProductPreActiveEvent pae = new ProductPreActiveEvent(p, item);
                            getProxy().getPluginManager().callEvent(pae);
                        }
                    }
                }
            }
        }, 1, TimeUnit.MINUTES);
    }


    public boolean canDebug() {
        return debug;
    }

    public void print(String s) {
        getProxy().getConsole().sendMessage(s);
    }
}
