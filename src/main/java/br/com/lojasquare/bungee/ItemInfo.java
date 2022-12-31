package br.com.lojasquare.bungee;

public class ItemInfo {
   private String player;
   private String produto;
   private String servidor;
   private String subServidor;
   private String grupo;
   private String codigo;
   private String status;
   private String cupom;
   private int statusID;
   private int dias;
   private int idEntrega;
   private int quantidade;
   private long atualizadoEm;

   public ItemInfo(String p) {
      this.player = p;
   }

   public long getUltimoUpdate() {
      return this.atualizadoEm;
   }

   public void setUltimoUpdate(long l) {
      this.atualizadoEm = l;
   }

   public String toString() {
      String a = "ItemInfo={player:" + this.player + "," + "produto:" + this.produto + "," + "servidor:" + this.servidor + "," + "subServidor:" + this.subServidor + "," + "grupo:" + this.grupo + "," + "codigo:" + this.codigo + "," + "status:" + this.status + ",statusID:" + this.statusID + "," + "dias:" + this.dias + "," + "idEntrega:" + this.idEntrega + "," + "quantidade:" + this.quantidade + "," + "lastUpdate:" + this.atualizadoEm + "," + "cupom:" + this.cupom + "}";
      return a;
   }

   public String getPlayer() {
      return this.player;
   }

   public void setPlayer(String s) {
      this.player = s;
   }

   public String getSubServidor() {
      return this.subServidor;
   }

   public String getProduto() {
      return this.produto;
   }

   public String getServidor() {
      return this.servidor;
   }

   public String getGrupo() {
      return this.grupo;
   }

   public String getCodigo() {
      return this.codigo;
   }

   public String getStatus() {
      return this.status;
   }

   public void setStatus(String s) {
      this.status = s;
   }

   public int getQuantidade() {
      return this.quantidade;
   }

   public int getIDEntrega() {
      return this.idEntrega;
   }

   public int getDias() {
      return this.dias;
   }

   public int getStatusID() {
      return this.statusID;
   }

   public String getCupom() {
      return this.cupom;
   }

   public void setCupom(String cupom) {
      this.cupom = cupom;
   }
}