package br.com.johnidouglas;



import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;



/**
 * Classe que declara um tipo de exce��o para manupular as respostas APDU
 * 
 * @author Johni Douglas Marangon 
 * @version 1.0 
 */  
class ErroRespostaAPDU extends Exception  {
	private static final long serialVersionUID = 1L;
	
	public ErroRespostaAPDU(String mensagem) {
		super( mensagem );
	}		
}



/**
 * Classe que possui rotinas uteis para manipular comandos e respostas recebidas de um applet
 * 
 * @author Johni Douglas Marangon
 * @version 1.0
 * 
 */
public class APDUUtils {

	

	/**
	 * Constante com os caracteres para convers�o de um hexadecimal em string.
	 *  
	 */	 
	private static final char[] HEXADECIMAIS = "0123456789ABCDEF".toCharArray(); 
	
	
	
	/**
	 * Convers�o de um valor inteiro para hexadecimal.
	 * 
	 * Fun��o utilizada para calcular o campo Lc do comanado APDU
	 * 
	 * @return representa��o String de um hexadecimal 
	 */
	private static final String parseIntToHex(int valor) {
		String hexadecimal = Integer.toHexString( ((int) valor ) & 0xFF ).toUpperCase();
		
		if (hexadecimal.length() == 2)
			return hexadecimal;
		else
			return "0" + hexadecimal; 	
	}
	
	
	
	/**
	 * Convers�o de um valor byte para hexadecimal
	 * 
	 * @return representa��o String de um hexadecimal
	 */
	private static final String parseByteToHex(byte valor) {				
			
		return String.valueOf( HEXADECIMAIS[ valor >>> 4 & 0xF ]) +
			   String.valueOf( HEXADECIMAIS[ valor & 0xF  ]) ;  		 
	}
	
	
	
	/**
	 * Convers�o de um valor em hexadecimal para byte
	 * O valor hexadecimal deve ser representado em uma string.
	 * 
	 * @return representa��o em byte
	 */
	private static final byte parseHexToByte(String valor) {
		return (byte) Short.parseShort( valor.toUpperCase(), 16);		
	}
	
	
	
	/**
	 * Conversao de um array de byte em hexadecimal
	 * 
	 * @return representa��o em string
	 */	
	private static final String parseBytesToHex(byte[] valor, boolean incluirLc) {
		StringBuffer string = new StringBuffer();
				
		if (incluirLc)
			string.append( parseIntToHex( valor.length ) );
		
		for (int indice = 0; indice < valor.length; ++indice) {						
			string.append( parseByteToHex( valor[indice] ) );			
		}		
					
		return string.toString();		
	}
	
	
	
	/**
	 * Convers�o de valores hexadecimais
	 * 
	 * @param valor valor que sera convertido para hexadecimal
	 * @param includeLc se ira incluir o campo Lc
	 * @return array de byte com a representa��o em hexadecimal
	 */
	private static final byte[] parseHexToBytes(String valor, boolean includeLc) {
						
		final char[] caracteres = parseBytesToHex( valor.getBytes(), includeLc ).toCharArray();  
               
        final byte[] retorno = new byte[ (caracteres.length / 2 ) ];
                
        for (int i = 0, j = 0; i < caracteres.length; i += 2, j++) {           
            retorno[j] = parseHexToByte( new String(new char[] {caracteres[i], caracteres[i + 1]}) );  
        }            
       
        return retorno;		
	}
	
	
	
	/**
	 * Convers�o de uma string em sua representa��o hexadecimal 
	 * N�o retorna o campo Lc
	 * 
	 * @return array de bytes
	 * 
	 */	
	
	private static final byte[] getDadosAPDU(String dados) {				
		return parseHexToBytes( dados, false );
	}
	
	
	
	/**
	 * Controi um comando APDU de sele��o
	 *  
	 * @param applet nome do applet instalado no cart�o
	 * @return Comando APDU de sele��o de um applet no cart�o
	 */
	public static final CommandAPDU getSelectAPDU(String applet) {
		
		CommandAPDU command = 
				new CommandAPDU(0x00, (byte) 0xA4, 0x04, 0x00, getDadosAPDU(applet) );
		
		return command;		
	}
	
	
	
	/**
	 * Controi um comando APDU para atualiza��o de dados no cart�o
	 * 
	 * @param CLA campo CLA do comando
	 * @param INS campo INS do comando
	 * @param P1 campo P1 do comando
	 * @param P2 campo P2 do camando
	 * @param dados dados que ser�o atualizados no cart�o
	 * @return Comando APDU para atualiza��o de dados no cart�o
	 * 
	 */
	public static final CommandAPDU getComandoAPDU(int CLA, int INS, int P1, int P2, String dados  ) {
		CommandAPDU comando = 
				new CommandAPDU(CLA, INS, P1, P2, getDadosAPDU( dados ) );
				
		return comando;		
	}
	
	
	
	/**
	 * Constroi o comando APDU para leitura de dados do cart�o
	 * 
	 * @param CLA campo CLA do comando
	 * @param INS campo INS do comando
	 * @param P1 campo P1 do comando
	 * @param P2 campo P2 do comando
	 * @param Le tamando da resposta esperado
	 * @return comando APDU para leitura de dados do cart�o
	 */
	public static final CommandAPDU getComandoAPDU(int CLA, int INS, int P1, int P2, int Le ) {
		CommandAPDU comando = new CommandAPDU( CLA, INS, P1, P2, Le );
		
		return comando;		
	}
	
	
	
	/**
	 * Rotina que converte uma resposta APDU.
	 * 
	 * @param resposta resposta APDU
	 * @return representa��o string de uma resposta recebida de uma applet
	 * @throws ErroRespostaAPDU caso o resultado do camando seja uma falha
	 */
	public static final String formataRespostaAPDU( ResponseAPDU resposta ) throws ErroRespostaAPDU {
		
		
		// Faz a verifica��o da resposta APDU. 
		// Caso a resposta n�o seja sucesso na execu��o do comando lan�a uma exce��o
		if ( resposta.getSW() != 0x9000 ) 
			throw new ErroRespostaAPDU( "Falha ao enviar o comando APDU" + 
					 " SW1: " + Integer.toHexString( resposta.getSW1() ) +
					 " SW2: " + Integer.toHexString( resposta.getSW2() ) );
		
			
		// Faz a conver��o dos dados recebidos do applet do cart�o.		
		String string = new String( resposta.getData()  );		
		return string.trim();	
		
	}	
	
}



