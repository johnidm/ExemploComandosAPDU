package br.com.johnidouglas;



import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;



/**
 * Classe que declara um tipo de exceção para manupular as respostas APDU
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
	 * Constante com os caracteres para conversão de um hexadecimal em string.
	 *  
	 */	 
	private static final char[] HEXADECIMAIS = "0123456789ABCDEF".toCharArray(); 
	
	
	
	/**
	 * Conversão de um valor inteiro para hexadecimal.
	 * 
	 * Função utilizada para calcular o campo Lc do comanado APDU
	 * 
	 * @return representação String de um hexadecimal 
	 */
	private static final String parseIntToHex(int valor) {
		String hexadecimal = Integer.toHexString( ((int) valor ) & 0xFF ).toUpperCase();
		
		if (hexadecimal.length() == 2)
			return hexadecimal;
		else
			return "0" + hexadecimal; 	
	}
	
	
	
	/**
	 * Conversão de um valor byte para hexadecimal
	 * 
	 * @return representação String de um hexadecimal
	 */
	private static final String parseByteToHex(byte valor) {				
			
		return String.valueOf( HEXADECIMAIS[ valor >>> 4 & 0xF ]) +
			   String.valueOf( HEXADECIMAIS[ valor & 0xF  ]) ;  		 
	}
	
	
	
	/**
	 * Conversão de um valor em hexadecimal para byte
	 * O valor hexadecimal deve ser representado em uma string.
	 * 
	 * @return representação em byte
	 */
	private static final byte parseHexToByte(String valor) {
		return (byte) Short.parseShort( valor.toUpperCase(), 16);		
	}
	
	
	
	/**
	 * Conversao de um array de byte em hexadecimal
	 * 
	 * @return representação em string
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
	 * Conversão de valores hexadecimais
	 * 
	 * @param valor valor que sera convertido para hexadecimal
	 * @param includeLc se ira incluir o campo Lc
	 * @return array de byte com a representação em hexadecimal
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
	 * Conversão de uma string em sua representação hexadecimal 
	 * Não retorna o campo Lc
	 * 
	 * @return array de bytes
	 * 
	 */	
	
	private static final byte[] getDadosAPDU(String dados) {				
		return parseHexToBytes( dados, false );
	}
	
	
	
	/**
	 * Controi um comando APDU de seleção
	 *  
	 * @param applet nome do applet instalado no cartão
	 * @return Comando APDU de seleção de um applet no cartão
	 */
	public static final CommandAPDU getSelectAPDU(String applet) {
		
		CommandAPDU command = 
				new CommandAPDU(0x00, (byte) 0xA4, 0x04, 0x00, getDadosAPDU(applet) );
		
		return command;		
	}
	
	
	
	/**
	 * Controi um comando APDU para atualização de dados no cartão
	 * 
	 * @param CLA campo CLA do comando
	 * @param INS campo INS do comando
	 * @param P1 campo P1 do comando
	 * @param P2 campo P2 do camando
	 * @param dados dados que serão atualizados no cartão
	 * @return Comando APDU para atualização de dados no cartão
	 * 
	 */
	public static final CommandAPDU getComandoAPDU(int CLA, int INS, int P1, int P2, String dados  ) {
		CommandAPDU comando = 
				new CommandAPDU(CLA, INS, P1, P2, getDadosAPDU( dados ) );
				
		return comando;		
	}
	
	
	
	/**
	 * Constroi o comando APDU para leitura de dados do cartão
	 * 
	 * @param CLA campo CLA do comando
	 * @param INS campo INS do comando
	 * @param P1 campo P1 do comando
	 * @param P2 campo P2 do comando
	 * @param Le tamando da resposta esperado
	 * @return comando APDU para leitura de dados do cartão
	 */
	public static final CommandAPDU getComandoAPDU(int CLA, int INS, int P1, int P2, int Le ) {
		CommandAPDU comando = new CommandAPDU( CLA, INS, P1, P2, Le );
		
		return comando;		
	}
	
	
	
	/**
	 * Rotina que converte uma resposta APDU.
	 * 
	 * @param resposta resposta APDU
	 * @return representação string de uma resposta recebida de uma applet
	 * @throws ErroRespostaAPDU caso o resultado do camando seja uma falha
	 */
	public static final String formataRespostaAPDU( ResponseAPDU resposta ) throws ErroRespostaAPDU {
		
		
		// Faz a verificação da resposta APDU. 
		// Caso a resposta não seja sucesso na execução do comando lança uma exceção
		if ( resposta.getSW() != 0x9000 ) 
			throw new ErroRespostaAPDU( "Falha ao enviar o comando APDU" + 
					 " SW1: " + Integer.toHexString( resposta.getSW1() ) +
					 " SW2: " + Integer.toHexString( resposta.getSW2() ) );
		
			
		// Faz a converção dos dados recebidos do applet do cartão.		
		String string = new String( resposta.getData()  );		
		return string.trim();	
		
	}	
	
}



