package com.lab31;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.security.MessageDigest;
import javacard.security.PrivateKey;
import javacard.security.PublicKey;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.CryptoException;
import javacard.security.Key;
import javacard.security.RSAPrivateKey;
import javacard.security.RSAPublicKey;


public class Task1 extends Applet {

			private static final byte HW_CLA = (byte) 0x80;
			
			private static final byte HW_INS_REC = (byte) 0x30;
			
			private static final byte HW_INS_SHA = (byte) 0x01;//SW2:00
			private static final byte HW_INS_SHA_256 = (byte) 0x04;//SW2:00
			private static final byte HW_INS_SHA_512 = (byte) 0x06;//SW2:03 CryptoException NO_SUCH_ALG
			/*
     		MessageDigest.ALG_MD5  		-- SW2:03 CryptoException NO_SUCH_ALG
     		MessageDigest.ALG_RIPEMD160 -- SW2:03 CryptoException NO_SUCH_ALG
     		MessageDigest.ALG_SHA 		-- SW2:00
     		MessageDigest.ALG_SHA3_224	-- SW2:03 CryptoException NO_SUCH_ALG
   			MessageDigest.ALG_SHA3_256	-- SW2:03 CryptoException NO_SUCH_ALG
   			MessageDigest.ALG_SHA3_384	-- SW2:03 CryptoException NO_SUCH_ALG
   			MessageDigest.ALG_SHA3_512	-- SW2:03 CryptoException NO_SUCH_ALG
     		MessageDigest.ALG_SHA_224	-- SW2:03 CryptoException NO_SUCH_ALG
     		MessageDigest.ALG_SHA_256	-- SW2:00
     		MessageDigest.ALG_SHA_384	-- SW2:03 CryptoException NO_SUCH_ALG
     		MessageDigest.ALG_SHA_512	-- SW2:03 CryptoException NO_SUCH_ALG
     		
     		KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_512 -- Seems working
			*/
			
			private static final byte HW_INS_RSA_GEN = (byte) 0x10;
			private static final byte HW_INS_RSA_GET_PRIV = (byte) 0x20;
			
			private byte [] receivedData;
			private byte [] hashedData;
			
			private MessageDigest mesDig;
			private KeyPair keyPair;

    private Task1(byte[] bArray, short bOffset, byte bLength) {
        register();
    } // end of the constructor

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // create a Wallet applet instance
        new Task1(bArray, bOffset, bLength);
    } // end of install method

    @Override
    public boolean select() {
        return true;
    }// end of select method

    @Override
    public void process(APDU apdu) {
        if (selectingApplet()) {
          return;
        }

     byte[] buffer = apdu.getBuffer();

     byte CLA = (byte) (buffer[ISO7816.OFFSET_CLA] & 0xFF);

     byte INS = (byte) (buffer[ISO7816.OFFSET_INS] & 0xFF);

     if (CLA != HW_CLA) {

       ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

     }

     switch (INS) {
      
     	//case HW_INS_REC:
     		//resendData(apdu,(byte)0);
     		//break;
     	case HW_INS_SHA:
     		try {
     			mesDig = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);

     		}catch(CryptoException e){
     			ISOException.throwIt(e.getReason());
     			//NO_SUCH_ALGORITHM = SW2:0x03 
     		}
     		resendHash(apdu, MessageDigest.LENGTH_SHA);
     		mesDig.reset();
     		break;
     	case HW_INS_SHA_256:
     		try {
     			mesDig = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
     		}catch(CryptoException e){
     			ISOException.throwIt(e.getReason());
     			//NO_SUCH_ALGORITHM = SW2:0x03 
     		}
     		resendHash(apdu, MessageDigest.LENGTH_SHA_256);
     		mesDig.reset();
     		break;
     	case HW_INS_SHA_512:
     		try {
     			mesDig = MessageDigest.getInstance(MessageDigest.ALG_SHA_512, false);
     		}catch(CryptoException e){
     			ISOException.throwIt(e.getReason());
     			//NO_SUCH_ALGORITHM = SW2:0x03 
     		}
     		resendHash(apdu, MessageDigest.LENGTH_SHA_512);
     		mesDig.reset();
     		break;
     	case HW_INS_RSA_GEN:
     		generateKeyPair();
     		returnKey(apdu,(byte)0x00);
     		break;
     	case HW_INS_RSA_GET_PRIV:
     		returnKey(apdu,(byte)0x01);
     		break;
     		
     	default:
     		ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
     }
     
   }
    void returnKey(APDU apdu, byte keyType){
    	byte[] buffer = apdu.getBuffer();
    	short keySize = 64;//64 bytes
    	byte[] keyMod = new byte[keySize];
    	byte[] keyExp= new byte[keySize];
    	
    	byte[] delimitter = new byte [4];
    	delimitter[0] = (byte)0x00;
    	delimitter[1] = (byte)0xFE;
    	delimitter[2] = (byte)0xED;
    	delimitter[3] = (byte)0x00;
    	
    	for(short i = 0; i<keySize ; i+=4){
    		keyMod[i] = 			keyExp[i] =				(byte)0xFE;
    		keyMod[(short)(i+1)] = 	keyExp[(short)(i+1)] =	(byte)0xED;
    		keyMod[(short)(i+2)] =	keyExp[(short)(i+2)] =	(byte)0xFA;
    		keyMod[(short)(i+3)] =	keyExp[(short)(i+3)] =	(byte)0xCE;
    	}
    	
    	if(keyType == 0x00){//public
    		RSAPublicKey retKey;
    		retKey=(RSAPublicKey)keyPair.getPublic();
    		
    		try{
        		retKey.getModulus(keyMod, (short)0 );
        	}catch(CryptoException e){
        		ISOException.throwIt(e.getReason());
    		}

        	try{
        		retKey.getExponent(keyExp, (short)0);
        	}catch(CryptoException e){
        		ISOException.throwIt(e.getReason());
    		}
    		
    	}else if(keyType == 0x01){//private
    		RSAPrivateKey retKey;
    		retKey=(RSAPrivateKey)keyPair.getPrivate();
    		
    		try{
        		retKey.getModulus(keyMod, (short)0 );
        	}catch(CryptoException e){
        		ISOException.throwIt(e.getReason());
    		}

        	try{
        		retKey.getExponent(keyExp, (short)0);
        	}catch(CryptoException e){
        		ISOException.throwIt(e.getReason());
    		}
    		
    	}else{//error
    		ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
    	}
    	
    	Util.arrayCopyNonAtomic(keyMod, (short) 0, buffer, (short) 0,
        		 keySize);
    	
    	Util.arrayCopyNonAtomic(delimitter, (short) 0, buffer, keySize,
       		 (short)4);
        
        Util.arrayCopyNonAtomic(keyExp, (short) 0, buffer, (short)(keySize + 4),
       		 keySize);
        apdu.setOutgoingAndSend((short) 0, (short)((keySize*2)+4));
    }
    
    void generateKeyPair(){
    	
    	try {
    		keyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_512);
    	}catch(CryptoException e){
    		ISOException.throwIt(e.getReason());
    		}
    	
    	try {
    		keyPair.genKeyPair();
    	}catch(CryptoException e){
    		ISOException.throwIt(e.getReason());
    		}
        
    }
    
    void resendHash(APDU apdu, byte hashLength){
    	byte[] buffer = apdu.getBuffer();
    	receiveData(apdu);
    	short recivedDataLength = (short) receivedData.length;
    	hashedData = new byte[(short)hashLength];
    	
    	try {
    		mesDig.doFinal(receivedData, (short)0, recivedDataLength, hashedData,
    			(short)0);
    	}catch(CryptoException e){
 			ISOException.throwIt(e.getReason());
 		}
    	
    	try {
        Util.arrayCopyNonAtomic(hashedData, (short) 0, buffer, (short) 0,
        		hashLength);
    	}catch(ArrayIndexOutOfBoundsException e){
    		ISOException.throwIt((short)0xAEAE);
    	}
        apdu.setOutgoingAndSend((short) 0, hashLength);
    	
    }
    
    void receiveData(APDU apdu) {

        byte[] buffer = apdu.getBuffer();

        short LC = apdu.getIncomingLength();

        short recvLen = apdu.setIncomingAndReceive();
        
        receivedData = new byte [recvLen];
        short receivedIdx = 0;

        short dataOffset = apdu.getOffsetCdata();        
        
        while (recvLen > 0) {
        	receivedData[receivedIdx]=buffer[dataOffset];
            recvLen = apdu.receiveBytes(dataOffset);
            receivedIdx++;
            dataOffset++;

        } 
    }
    
   
}
