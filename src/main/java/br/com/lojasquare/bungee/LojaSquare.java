package br.com.lojasquare.bungee;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

public class LojaSquare {

   private int connectionTimeout = 1500;
   private int readTimeout = 3000;
   private String credencial;
   private String ipMaquina;
   private boolean debug = false;

   public static void main(String[] args) {
      LojaSquare ls = new LojaSquare();
      ls.setCredencial("Lo1nvWtvhaQmOZvhsYPDeQQKE8SKQ2");
      ls.setDebug(true);
      List<ItemInfo> li = ls.getTodasEntregas();
      if (li != null && li.size() != 0) {
         print(((ItemInfo)li.get(0)).toString());
         Iterator var4 = li.iterator();

         while(var4.hasNext()) {
            ItemInfo ii = (ItemInfo)var4.next();
            print(ii.toString());
            print("Nada para entregar ao player Trow_Games.");
         }

      } else {
         print("OPA!");
      }
   }

   public String getCredencial() {
      return this.credencial;
   }

   public void setCredencial(String keyAPI) {
      this.credencial = keyAPI;
   }

   public void setConnectionTimeout(int milisec) {
      this.connectionTimeout = milisec;
   }

   public void setReadTimeout(int milisec) {
      this.readTimeout = milisec;
   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public boolean canDebug() {
      return this.debug;
   }

   public int getConnectionTimeout() {
      return this.connectionTimeout;
   }

   public int getReadTimeout() {
      return this.readTimeout;
   }

   public boolean updateDelivery(ItemInfo ii) {
      return ii == null ? false : this.update(String.format("/v1/queue/%s/%d", ii.getPlayer(), ii.getIDEntrega()));
   }

   public List<ItemInfo> getTodasEntregas() {
      String player = "*";
      ArrayList itens = new ArrayList();

      try {
         String result = this.get(String.format("/v1/queue/%s", player));
         if (result.startsWith("LS-")) {
            return itens;
         } else {
            JsonObject job = (new JsonParser()).parse(result).getAsJsonObject();

            for(int i = 1; i <= job.entrySet().size(); ++i) {
               try {
                  ItemInfo ii = (ItemInfo)(new Gson()).fromJson(job.getAsJsonObject(String.valueOf(i)), ItemInfo.class);
                  itens.add(ii);
               } catch (Exception var7) {
                  print("[LojaSquare] Nao foi possivel processar o item " + job.getAsJsonObject(String.valueOf(i)).toString() + ". Erro: " + var7.getMessage());
               }
            }

            return itens;
         }
      } catch (Exception var8) {
         return itens;
      }
   }

   public List<ItemInfo> getEntregasPlayer(String player) {
      ArrayList itens = new ArrayList();

      try {
         String result = this.get(String.format("/v1/queue/%s", player));
         if (result.startsWith("LS-")) {
            return itens;
         } else {
            JsonObject job = (new JsonParser()).parse(result).getAsJsonObject();

            for(int i = 1; i <= job.entrySet().size(); ++i) {
               try {
                  ItemInfo ii = (ItemInfo)(new Gson()).fromJson(job.getAsJsonObject(String.valueOf(i)), ItemInfo.class);
                  itens.add(ii);
               } catch (Exception var7) {
                  print("[LojaSquare] Nao foi possivel processar o item " + job.getAsJsonObject(String.valueOf(i)).toString() + ". Erro: " + var7.getMessage());
               }
            }

            return itens;
         }
      } catch (Exception var8) {
         return itens;
      }
   }

   public String get(String endpoint) {
      HttpsURLConnection c = null;
      int statusCode = 0;

      try {
         StringBuilder sb2 = new StringBuilder();
         URL u = new URL(sb2.append("https://api.lojasquare.com.br/").append(endpoint).toString());
         c = (HttpsURLConnection)u.openConnection();
         c.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0) lojasquare");
         c.setRequestMethod("GET");
         c.setRequestProperty("Authorization", this.getCredencial());
         c.setRequestProperty("Content-Type", "application/json");
         c.setUseCaches(false);
         c.setAllowUserInteraction(false);
         c.setConnectTimeout(this.getConnectionTimeout());
         c.setReadTimeout(this.getReadTimeout());
         c.connect();
         statusCode = c.getResponseCode();
         if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = br.readLine()) != null) {
               sb.append(line).append("\n");
            }

            br.close();
            String var10 = sb.toString();
            return var10;
         }
      } catch (IOException var21) {
         if (this.canDebug()) {
            var21.printStackTrace();
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, (String)null, var21);
         } else {
            this.msgConsole("§4[LojaSquare] §cErro ao tentar conexao com o site. Erro: §a" + var21.getMessage());
         }

         if (c != null) {
            try {
               c.disconnect();
            } catch (Exception var20) {
               if (this.canDebug()) {
                  var20.printStackTrace();
                  Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, (String)null, var20);
               } else {
                  this.msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + var20.getMessage());
               }
            }
         }
      } finally {
         if (c != null) {
            try {
               c.disconnect();
            } catch (Exception var19) {
               if (this.canDebug()) {
                  var19.printStackTrace();
                  Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, (String)null, var19);
               } else {
                  this.msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + var19.getMessage());
               }
            }
         }

      }

      System.out.println(statusCode);
      return "LS-" + this.getResponseByCode(statusCode);
   }

   public boolean update(String endpoint) {
      HttpsURLConnection c = null;
      int statusCode = 0;

      label158: {
         try {
            StringBuilder sb = new StringBuilder();
            URL u = new URL(sb.append("https://api.lojasquare.com.br/").append(endpoint).toString());
            c = (HttpsURLConnection)u.openConnection();
            c.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0) lojasquare");
            c.setRequestMethod("PUT");
            c.setRequestProperty("Authorization", this.getCredencial());
            c.setRequestProperty("Content-Type", "application/json");
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(this.getConnectionTimeout());
            c.setReadTimeout(this.getReadTimeout());
            c.connect();
            statusCode = c.getResponseCode();
            if (statusCode != 200 && statusCode != 201 && statusCode != 204) {
               break label158;
            }
         } catch (IOException var17) {
            if (this.canDebug()) {
               var17.printStackTrace();
               Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, (String)null, var17);
            } else {
               this.msgConsole("§4[LojaSquare] §cErro ao tentar conexao com o site. Erro: §a" + var17.getMessage());
            }

            if (c != null) {
               try {
                  c.disconnect();
               } catch (Exception var16) {
                  if (this.canDebug()) {
                     var16.printStackTrace();
                     Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, (String)null, var16);
                  } else {
                     this.msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + var16.getMessage());
                  }
               }
            }
            break label158;
         } finally {
            if (c != null) {
               try {
                  c.disconnect();
               } catch (Exception var15) {
                  if (this.canDebug()) {
                     var15.printStackTrace();
                     Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, (String)null, var15);
                  } else {
                     this.msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + var15.getMessage());
                  }
               }
            }

         }

         return true;
      }

      print(this.getResponseByCode(statusCode));
      return false;
   }

   public String getResponseByCode(int i) {
      String msg = "";
      switch(i) {
      case 0:
         msg = "[LojaSquare] §cServidor sem conexao com a internet.";
         break;
      case 401:
         msg = "[LojaSquare] §cConexao nao autorizada! Por favor, confira se a sua credencial esta correta.";
         break;
      case 404:
         msg = "[LojaSquare] §cNao foi encontrado nenhum registro para a requisicao efetuada.";
         break;
      case 405:
         msg = "[LojaSquare] §cErro ao autenticar sua loja! Verifique se sua assinatura e credencial estao ativas!";
         break;
      case 406:
         msg = "[LojaSquare] §cNao foi executada nenhuma atualizacao referente ao requerimento efetuado.";
         break;
      case 409:
         msg = "[LojaSquare] §cO IP enviado e diferente do que temos em nosso Banco de Dados. IP da sua Maquina: §a" + this.getIpMaquina();
         break;
      case 423:
         msg = "[LojaSquare] §cO IP da maquina do seu servidor ou a sua key-api foram bloqueados.";
         break;
      default:
         msg = "[LojaSquare] §cProvavel falha causada por entrada de dados incompativeis com o requerimento efetuado. Status Code: " + i;
      }

      return msg;
   }

   public static int parseInt(String a) {
      return Integer.parseInt(a);
   }

   public static void print(String a) {
      System.out.println(a);
   }

   public static void print(int a) {
      System.out.println(a);
   }

   public static void print(double a) {
      System.out.println(a);
   }

   public void msgConsole(String s) {
      ProxyServer.getInstance().getConsole().sendMessage(s);
   }

   public String getIpMaquina() {
      if (this.ipMaquina == null) {
         String getIP = this.get("/v1/autenticar/ip");
         if (getIP.length() > 20) {
            this.ipMaquina = "não identificado.";
         } else {
            this.ipMaquina = getIP;
         }
      }

      return this.ipMaquina;
   }

   public void setIpMaquina(String ipMaquina) {
      this.ipMaquina = ipMaquina;
   }
}