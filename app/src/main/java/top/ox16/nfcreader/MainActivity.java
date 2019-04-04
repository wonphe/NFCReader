package top.ox16.nfcreader;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent pi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //初始化PendingIntent
        // 初始化PendingIntent，当有NFC设备连接上的时候，就交给当前Activity处理
        pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // 新建IntentFilter，使用的是第二种的过滤机制
        //        tagDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        //        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);

        if (mNfcAdapter == null) {
            Toast.makeText(MainActivity.this, "设备不支持NFC", Toast.LENGTH_LONG).show();
        }

        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "请在系统设置中先启用NFC功能", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 当前app正在前端界面运行，这个时候有intent发送过来，那么系统就会调用onNewIntent回调方法，将intent传送过来
        // 我们只需要在这里检验这个intent是否是NFC相关的intent，如果是，就调用处理方法
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            processIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(this, pi, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        String uid = "";
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] techList = tagFromIntent.getTechList();
        //打印卡的技术列表
        StringBuilder str = new StringBuilder("Tech List:\n");
        for (String tech : techList) {
            str.append(tech).append("\n");
        }
        Toast.makeText(this, str.toString(), Toast.LENGTH_LONG).show();
        // 判断NFC标签的数据类型（通过Ndef.get方法）
//        Ndef ndef = Ndef.get(tagFromIntent);
//        String mTagText = ndef.getType() + "\n max size:" + ndef.getMaxSize() + " bytes\n\n";
//        Toast.makeText(this, mTagText, Toast.LENGTH_LONG).show();

        byte[] aa = tagFromIntent.getId();
        String hexStr = bytesToHexString(aa);
        uid += hexStr;//获取卡的UID
        if (hexStr != null) {
            String decStr = hexToDecString(hexStr);
            TextView txtDecUID = findViewById(R.id.txtDecUID);
            txtDecUID.setText(decStr);
        }
        TextView txtUID = findViewById(R.id.txtUID);
        txtUID.setText(uid);
    }

    /**
     * 十六进制倒转后转换为十进制
     *
     * @param hex 十六进制字符串
     */
    private String hexToDecString(String hex) {
        StringBuilder hexStr = new StringBuilder("");
        String[] hexes = hex.split(":");
        for (int i = hexes.length - 1; i >= 0; i--) {
            hexStr.append(hexes[i]);
        }
        return new BigInteger(hexStr.toString(), 16).toString(10);
    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
            if (i != src.length - 1)
                stringBuilder.append(":");
        }
        return stringBuilder.toString().toUpperCase();
    }
}
