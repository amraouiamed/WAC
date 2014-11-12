package Client;

import javax.swing.JFrame;


public class MainClient
{
   public static void main( String[] args )
   {
      Joueur application; 

      if ( args.length == 0 )
         application = new Joueur( "127.0.0.1" );  //localhost
      else
         application = new Joueur( args[ 0 ] ); 

      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      application.runClient();  
   }  
}  





