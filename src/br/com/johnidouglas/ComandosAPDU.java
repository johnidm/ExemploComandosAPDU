package br.com.johnidouglas;


import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.TerminalFactory;


public class ComandosAPDU {
		
	

	public static void main(String[] args) throws CardException, ErroRespostaAPDU {
			
		
		// Faz a conexão com o leitor de Smart Card
		Card card = TerminalFactory.getDefault().terminals().list().get(0).connect("*");
		System.out.println("Conectado no terminal: " + card + " ATR: " + card.getATR());		
		CardChannel channel = card.getBasicChannel();		
	
		
		// Comando para seleção do applet "nexus7"  
		System.out.println( 	
				channel.transmit(						
						APDUUtils.getSelectAPDU("nexus7")												
						//new CommandAPDU( teste						
						//new CommandAPDU( 0x00, 0xA4, 0x04, 0x00, new byte[] { 0x6E, 0x65, 0x78, 0x75, 0x73, 0x37 }						
						//new CommandAPDU( select						
				) ) ;
		
		// Invoca uma instrução no applet para gravar o dado "hello word"
		System.out.println( 	
				channel.transmit(						
						APDUUtils.getComandoAPDU(0xB0, 0x10, 0x00, 0x00, "hello word1111")						
				) ) ;	
		
			
		// Invoca uma instrução no applet para retorno o dado gravado
		System.out.println( 	
				
				//Arrays.toString( channel.transmit(						
				//		APDUUtils.getComandoAPDU(0xB0, 0x11, 0x00, 0x00, 0x05) ).getData() )
				
				APDUUtils.formataRespostaAPDU( 
				
						channel.transmit(			
									APDUUtils.getComandoAPDU(0xB0, 0x11, 0x00, 0x00, 0x05) )
									
									)
				
		) ;
						
	}
		
	
}
