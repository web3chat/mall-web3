package com.fzm.mall.third.chain.wallet;

import com.fzm.mall.third.chain.entity.IndexEnum;
import com.fzm.mall.third.chain.entity.Wallet;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import static org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT;

public class ETHUtils {

    public static Wallet newWallet(int uid, IndexEnum indexEnum) {
        // m / purpose' / coin_type' / account' / change / index
        // purpose: 固定为44（或49、84等），表示遵循BIP44（或BIP49、BIP84）规范
        // coin_type: 币种类型，比特币为0，以太坊为60
        // account: 账户索引，从0开始。允许创建多个账户，每个账户独立管理
        // change: 0表示外部链（用于接收地址），1表示内部链（用于找零地址）。外部链地址是公开给别人的，内部链地址通常不公开
        // index: 地址索引，从0开始递增

        final int[] path = {44 | HARDENED_BIT, 60 | HARDENED_BIT, uid | HARDENED_BIT, 0, indexEnum.getIndex()};

        Bip32ECKeyPair bip32KeyPair = Bip32ECKeyPair.generateKeyPair(WalletSeed.ethSeed);
        Bip32ECKeyPair bip44Keypair = Bip32ECKeyPair.deriveKeyPair(bip32KeyPair, path);

        String address = Numeric.prependHexPrefix(Keys.getAddress(bip44Keypair));
        String privateKey = Numeric.toHexStringNoPrefixZeroPadded(bip44Keypair.getPrivateKey(), 64);

        return new Wallet(address, privateKey);
    }
}
