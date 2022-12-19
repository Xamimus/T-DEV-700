package com.api.payment.controller;

import com.api.bank.manager.BankManager;
import com.api.bank.model.entity.QrCheck;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/*
* Class creation of the Qr-code used on the TPE.
*/

@RestController
@RequestMapping(path = "/payment/qr-code")
public class QrCodeController {

    @Value("${default.qrcode.secret}")
    private String key;
    private static SecretKeySpec secretKey;
    private final BankManager qrCheck;

    @Autowired()
    public QrCodeController(BankManager qrCheck) {
        this.qrCheck = qrCheck;
    }

    @RequestMapping(value = {"/", ""}, method = RequestMethod.POST)
    public void createQrcode(@RequestBody QrCheck qrCheck) {
        try {
            QrCheck model = new QrCheck(qrCheck.getSoldAmount(), qrCheck.getNbDayOfValidity());
            String token = model.getSoldAmount() + ":" + model.getNbDayOfValidity() + ":" + model.getCreatedAt();
            String encryptedString = encrypt(token, key);

            model.setCheckToken(encryptedString);
            //model = qrCheck.byQrCheck(model);
            if(model != null) {
                generateQrcode(encryptedString);
            }

        } catch (IOException | WriterException e) {
            System.out.println(e);
        }
    }

    public static void setKey(final String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(final String strToEncrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }

    public static String[] decrypt(final String strToDecrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String decrypt = new String(cipher.doFinal(Base64.getDecoder()
                    .decode(strToDecrypt)));

            String[] arrOfStr = decrypt.split(":");
            System.out.println(Arrays.toString(arrOfStr));

            return arrOfStr;
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }

    public static void generateQrcode(String check) throws WriterException, IOException
    {
        try {
            UUID uuid = UUID.randomUUID();
            String str = "qr-code-" + uuid;
            String path = "/qr-code/" + str + ".jpg";
            String charset = "UTF-8";

            Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<>();
            hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            qrcode(check, path, charset, 250, 250);

        } catch (Exception e) {
            System.out.println("An error occurred : " + e);
        }
    }

    public static void qrcode(String data, String path, String charset, int h, int w) throws WriterException, IOException
    {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
    }

    @GetMapping(value = "/1")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getImageDynamicType() {
        MediaType contentType = MediaType.IMAGE_JPEG;
        InputStream in = null;
        try {
            in = new FileInputStream("../../../../qr-code/data.jpg");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<InputStreamResource>((InputStreamResource) null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().contentType(contentType).body(new InputStreamResource(in));
    }
}

