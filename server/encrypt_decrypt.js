const CryptoJS = require('crypto-js');

function encrypt (data){
    var key  = CryptoJS.enc.Latin1.parse('1234567823435678');
    var iv   = CryptoJS.enc.Latin1.parse('1234567282345678');  
    var encrypted = CryptoJS.AES.encrypt(
        data,
        key,
        {iv:iv,mode:CryptoJS.mode.CBC,padding:CryptoJS.pad.Pkcs7
    });
    return encrypted
}

function decrypt(data){
    var decrypted = CryptoJS.AES.decrypt(data,key,
    {iv:iv, 
        mode:CryptoJS.mode.CBC,
        padding:CryptoJS.pad.Pkcs7}
    );
    console.log('decrypted: '+decrypted.toString(CryptoJS.enc.Utf8));
    return decrypted.toString(CryptoJS.enc.Utf8);
}

