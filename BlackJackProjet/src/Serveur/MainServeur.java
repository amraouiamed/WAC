package Serveur;

import javax.swing.JFrame;

public class MainServeur
{
   public static void main( String[] args )
   {
      Croupier application = new Croupier();  
      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      application.runDeal();  
   }  
}  