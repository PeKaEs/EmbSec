/**
 * 
 */
package com.example.MyHello;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class HelloWorld extends Applet {

	private static final byte[] helloWorld = { (byte) 'H', (byte) 'e',

			(byte) 'l', (byte) 'l', (byte) 'o', (byte) ' ', (byte) 'W',

			(byte) 'o', (byte) 'r', (byte) 'l', (byte) 'd', };
	
	private static final byte[] embSec = { (byte) 'E', (byte) 'm',
			(byte) 'b', (byte) 'e', (byte) 'd', (byte) 'd', (byte) 'e',
			(byte) 'd', (byte) ' ', (byte) 'S', (byte) 'e', (byte) 'c', (byte) 'u'
			, (byte) 'r', (byte) 'i', (byte) 't', (byte) 'y', (byte) ' ', (byte) 'S'
			, (byte) 'y', (byte) 's', (byte) 't', (byte) 'e', (byte) 'm', (byte) 's'};

			private static final byte HW_CLA = (byte) 0x80;

			private static final byte HW_INS = (byte) 0x00;
			
			private static final byte HW_INS_EMB = (byte) 0x10;
			
			private static final byte HW_INS_REV = (byte) 0x20;

    private HelloWorld(byte[] bArray, short bOffset, byte bLength) {

        register();

    } // end of the constructor

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // create a Wallet applet instance
        new HelloWorld(bArray, bOffset, bLength);
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

     case HW_INS:

      getHelloWorld(apdu);

      break;
      
     case HW_INS_EMB:
    	 getEmb(apdu);
     break;
     
     case HW_INS_REV:
    	 getReversedAPDU(apdu);
    break;

     default:

      ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);

     }
                }
    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        short i = 0;
        int jj =  (short) array.length - 1;
        short j = (short) jj;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
    
    private static void getReversedAPDU(APDU apdu){
    	
    	byte[] buffer = apdu.getBuffer();

        short length = (short) buffer.length;
        
        reverse(buffer);

       apdu.setOutgoingAndSend((short) 0, length);
    }
    
    private void getEmb(APDU apdu){
    	
    	byte[] buffer = apdu.getBuffer();

        short length = (short) embSec.length;

       Util.arrayCopyNonAtomic(embSec, (short) 0, buffer, (short) 0,

       (short) length);

       apdu.setOutgoingAndSend((short) 0, length);
 	   
    }

   private void getHelloWorld(APDU apdu) {

         byte[] buffer = apdu.getBuffer();

         short length = (short) helloWorld.length;

        Util.arrayCopyNonAtomic(helloWorld, (short) 0, buffer, (short) 0,

        (short) length);

        apdu.setOutgoingAndSend((short) 0, length);

          }
   

   }
