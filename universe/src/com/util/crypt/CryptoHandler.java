package com.util.crypt;

import java.math.BigInteger;

public class CryptoHandler {

    private String key;

    public CryptoHandler() {
        this.key="abc";
    }

    public byte[] xor(byte[] P, byte[] k) {
        int j=0;
        if (k.length==0)
            return xor(key.getBytes(),P);

        byte[] C = new byte[P.length];

        for (int i=0; i<P.length; i++) {
            if (j>=k.length)
                j=0;
            C[i] = (byte)(P[i]^k[j++]);
          //  System.out.println("byte="+(int)(C[i]& 0xFF));

        }
        return C;
    }

    public  byte[] cipherBlockEnc(byte[] m, byte[] k) {
        if (m.length>k.length)
            return m;
        byte[] c = new byte[m.length];
        for (int i=0; i<m.length; i++) {
            c[i]=(byte)((m[i]+(i*i+i+1)*k[i])%256);
        }
        return c;
    }
    public  byte[] cipherBlockDec(byte[] c, byte[] k) {
        if (c.length>k.length)
            return c;
        byte[] m = new byte[c.length];
        for (int i=0; i<c.length; i++) {
            m[i]=(byte)((c[i]-(i*i+i+1)*k[i])%256);
        }
        return m;
    }



    public byte[] cbcEncrypt(byte[] P, byte[] k, byte[] iv) {
        int output_size = 16+P.length+16-P.length%16;//1024 max (64 blocks of 16 bytes) + 1 block for info
        byte[] M = new byte[output_size];
        byte[] C = new byte[output_size];
       // System.out.print("msg={");
        int mlen = P.length;
        String infoTxt = "nil;;"+P.length+";;nei";
        while (infoTxt.length()!=16) {
            infoTxt+=" ";
        }
        int q = 0;
        for (int i=0; i<16; i++) {
            M[i]=(byte)infoTxt.charAt(i);
            q++;
        }
        for (int i=0; i<P.length; i++) {
            M[i+16]=P[i];
            q++;
        }
        while (q<output_size) {
            M[q++]=(new String(" ")).getBytes()[0];
        }
        for (int i=0; i<output_size; i++) {
            C[i]=0;
            //System.out.print(" "+((int)(M[i]&0xFF)));
        }

        int numBlocks = output_size/16;
        int j=0;
        byte[][] mblock = new byte[numBlocks][16];
        byte[][] cblock = new byte[numBlocks][16];
        for (int i=0; i<output_size; i++) {
            if (i%16==0&&i>0)
                j++;
            if (j<mblock.length) {
                mblock[j][i%16]=M[i];
            }
        }
        for (int t=0; t<mblock.length;t++) { //block cipher
            if (t==0)
                cblock[t]=cipherBlockEnc(xor(mblock[t],iv),k);
            else
                cblock[t]=cipherBlockEnc(xor(mblock[t],cblock[t-1]),k);
        }

        j=0;
       // System.out.print("enc = {");
        for (int i=0; i<numBlocks; i++) { //encrypt
            for (int z=0; z<16; z++) {
                C[j]=cblock[i][z];
                int x = ((int)(C[j]&0xFF));
              //  System.out.print(" "+x);
                j++;
            }
        }
       // System.out.println("}\n");

        for (int t=0; t<mblock.length;t++) { //decrypt
           // mblock[t]=xor(cblock[t],k);
            if (t==0)
                mblock[t]=xor(cipherBlockDec(cblock[t],k),iv);
            else {
                mblock[t]=xor(cipherBlockDec(cblock[t],k),cblock[t-1]);
            }

        }

        return C;
    }

    public byte[] cbcDecrypt(byte[] C, byte[] k, byte[] iv) {
        int output_size = C.length;//1024 max (64 blocks of 16 bytes)
        byte[] M = new byte[output_size];
        byte[] Ctemp = new byte[output_size];
        for (int i=0; i<output_size; i++) {
            Ctemp[i]=C[i];
            M[i]=0;
        }

        int numBlocks = output_size/16;
        int j=0;
        byte[][] mblock = new byte[numBlocks][16];
        byte[][] cblock = new byte[numBlocks][16];

        for (int i=0; i<output_size; i++) {
            if (i%16==0&&i>0)
                j++;
            if (j<cblock.length) {
                cblock[j][i%16]=Ctemp[i];
            }
        }

        byte[] a = new byte[16];
        a = cipherBlockDec(cblock[0],k);
        a = xor(a,iv);
        String x = new String(a);
        int mlen = Integer.parseInt(x.split(";;")[1]);

        for (int t=0; t<mblock.length;t++) { //block cipher
            if (t==0) {
                mblock[t] = cipherBlockDec(cblock[t], k);
                mblock[t] = xor(mblock[t],iv);
            }
            else {
                mblock[t] = cipherBlockDec(cblock[t], k);
                mblock[t] = xor(mblock[t],cblock[t-1]);
            }
        }

        byte[] decm = new byte[mlen];
        for (int i=0; i<decm.length;i++) {
            decm[i]=mblock[1+i/16][i%16];
        }

        return decm;
    }





    //elgamal

    private static int priv_a = 7; //private key
    //p=251?
    //public key = {p,g,h} for p=order of group, g=generator, h=g^x for random x in G

    public static int pub_g = 3;
    public static int pub_p = 251;
    public static int pub_h = 343; //7^2 = g^2; 7^3=343


    //im giving up this doesnt work
    public byte[] elGamalEncrypt(int b, byte[] M) {

        int c = (int)Math.pow(b,priv_a)%pub_p;
        int r = 2;
        int crmodp = (int)Math.pow(c,r)%pub_p;

        byte[] ans = new byte[M.length*2];
        System.out.print("M = [ ");
        for (byte m: M) {
            System.out.print(String.format("%02X", m)+" ");
        }
        System.out.println("]");
        System.out.print("Enc = [ ");
        int j=0;
        for (int i=0; i<ans.length; i++) {
            if (i%2==0) {
                ans[i]=(byte)(((int)Math.pow(pub_g,b))%pub_p); //b=y; g^b % p
            } else {
              //  ans[i]=(byte)((crmodp*M[j++])%pub_p);
                ans[i]=(byte)((int)Math.pow(pub_g,priv_a*b)*M[j++]%pub_p);//g^ab * M % p
                System.out.print(String.format("%02X", ans[i])+" ");
            }
        }
        System.out.println("]");
        return ans;
    }

    public byte[] elGamalDecrypt(byte[] C) {
        byte[] dec = new byte[C.length/2];
        int j=0;
        System.out.print("M = [ ");
       // int s = 0;
       // int s1 = 0;
        int ss = 1;
        for (int i=0; i<C.length; i++) {
            if (i%2==0) {
               // c1 = C[i]; //c1 = b^r%p
                int c1 = C[i];
                ss = (int)Math.pow(c1,priv_a)%pub_p;
               // s = (int)Math.pow(c1,priv_a);
               // int s = ((byte)Math.pow(pub_h,b))%pub_p;
               // s1 = (int)Math.pow(c1,pub_p-priv_a);
            } else {
                int c2 = C[i];
                int z=1;
                while (z*ss%pub_p!=1) {
                    z++;
                }
                int adl = c2*z%pub_p;
                byte m = (byte)adl;
                /*byte x = (byte)((((int)Math.pow(c1,priv_a))%pub_p)*C[i]);
                while ((x*z)%pub_p!=1) {
                    z++;
                }
                //System.out.println("z="+z);*/
                dec[j]=m;
                System.out.print(String.format("%02X", dec[j++])+" ");
                //System.out.println("byte=");
            }
        }
        System.out.println("]");
        return dec;
    }



    //rsa


    //keygen: p,q large primes roughly same size
    //n=pq
    //theta=(q-1)(p-1)
    //e: gcd(e,theta)=1 (usually e=3?) 1<e<theta
    //d: 1<d<theta such that e*d congruent to 1%theta ; (e*d)%theta = 1%theta = 1
    //public key: (e,n)
    //private key: d

    //try with p=199, q=193
    public int privateKey(int e, int p, int q) {
        int n = p*q;
        int theta = find_theta_value(p,q);//lcm(q-1,p-1);
       // System.out.println("lcm="+theta);
        for (int d=1; d<theta; d++) {
            if ((e*d)%theta == 1) {
                return d;
            }
        }
        return 0;
    }

    public int find_theta_value(int p, int q) {
        return (p-1)*(q-1);//lcm(q-1,p-1);
    }

    public int find_e_value(int p, int q) {
        int theta = find_theta_value(p,q);
        for (int i=2; i<theta; i++) {
            if (gcd(i,theta)==1) {
                //System.out.println("e="+i);
                return i;
            }
        }
        return 0;
    }

    //copied this method from https://www.baeldung.com/java-least-common-multiple
    public static int gcd(int number1, int number2) {
        if (number1 == 0 || number2 == 0) {
            return number1 + number2;
        } else {
            int absNumber1 = Math.abs(number1);
            int absNumber2 = Math.abs(number2);
            int biggerValue = Math.max(absNumber1, absNumber2);
            int smallerValue = Math.min(absNumber1, absNumber2);
            return gcd(biggerValue % smallerValue, smallerValue);
        }
    }

    //copied this method from https://www.baeldung.com/java-least-common-multiple
    public static int lcm(int number1, int number2) {
        if (number1 == 0 || number2 == 0) {
            return 0;
        }
        int absNumber1 = Math.abs(number1);
        int absNumber2 = Math.abs(number2);
        int absHigherNumber = Math.max(absNumber1, absNumber2);
        int absLowerNumber = Math.min(absNumber1, absNumber2);
        int lcm = absHigherNumber;
        while (lcm % absLowerNumber != 0) {
            lcm += absHigherNumber;
        }
        return lcm;
    }

    //C = M^e % N
    public byte[] rsaEncrypt(long e, long n, byte[] M) {
        byte[] C = new byte[M.length*4];
       // System.out.print("Enc = [ ");
        String outp = "";
        for (int i=0; i<M.length; i++) {
            int mi = (int)M[i];
            long encr = ((long)Math.pow(mi,e)%n);
            String xs = String.format("%04X", encr);
           // System.out.print(String.format("%04X", encr)+" ");
            byte[] ad = xs.getBytes();
            for (int j=0; j<ad.length; j++) {
                C[i*4+j]=ad[j];
            }
        }
        return C;
    }

    //M = C^d % n = (M^e % n)^d % n = M^ed % n = M
    public byte[] rsaDecrypt(int p, int q, byte[] C) {
        int d = privateKey(find_e_value(p,q),p,q);
        int n = p*q;
        byte[] M = new byte[C.length/4];
        for (int i=0; i<M.length; i++) {
           // int ci = (int)C[i];
            byte[] ffs = new byte[4];
            int k=0;
            for (int j=4*i; j<4*i+4; j++) {
                if (j>=C.length)
                    break;
                ffs[k++]=C[j];
            }
            String x = new String(ffs);
            if (x.length()>0) {
                try {
                    int omg = Integer.parseInt(x, 16);
                    BigInteger zomg = new BigInteger("" + omg);
                    BigInteger dd = new BigInteger(""+d);
                    BigInteger nn = new BigInteger(""+n);
                    BigInteger zmg = zomg.modPow(dd, nn);
                    M[i]=(byte)(Integer.parseInt(String.format("%02X", zmg),16));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return M;
    }

    public String bytesToHex(byte[] convertMeDaddy) {
        String buildMeDaddy = "";
        for (byte b: convertMeDaddy) {
            buildMeDaddy+=String.format("%02X", b)+" ";
        }
        return buildMeDaddy.substring(0,buildMeDaddy.length()-1);
    }

    public byte[] hexToBytes(String hex) {
        String[] minihex = hex.split(" ");
        byte[] out = new byte[minihex.length];
        int i=0;
        for (String x: minihex) {
            out[i++]=(byte)Integer.parseInt(x,16);
        }
        return out;
    }


}