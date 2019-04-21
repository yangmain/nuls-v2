/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.utils.TimeUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */

public class TxUnconfirmed extends BaseNulsData {

    private String address;


    private int assetChainId;

    private int assetId;

    private byte[] fromNonce = LedgerConstant.INIT_NONCE_BYTE;
    private byte[] nonce = LedgerConstant.INIT_NONCE_BYTE;
    private byte[] nextNonce = LedgerConstant.INIT_NONCE_BYTE;
    private BigInteger amount = BigInteger.ZERO;


    public TxUnconfirmed() {
        super();
    }

    public TxUnconfirmed(String address, int assetChainId, int assetId, byte[] fromNonce,byte[] nonce,BigInteger amount) {
        this.address = address;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
        this.fromNonce = fromNonce;
        this.nonce = nonce;
        this.amount = amount;
    }


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeUint16(assetChainId);
        stream.writeUint16(assetId);
        stream.write(fromNonce);
        stream.write(nonce);
        stream.write(nextNonce);
        stream.writeBigInteger(amount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readString();
        this.assetChainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.fromNonce = byteBuffer.readBytes(8);
        this.nonce = byteBuffer.readBytes(8);
        this.nextNonce = byteBuffer.readBytes(8);
        this.amount = byteBuffer.readBigInteger();
    }

    @Override
    public int size() {
        int size = 0;
        //address
        size += SerializeUtils.sizeOfString(address);
        //asset chainId
        size += SerializeUtils.sizeOfInt16();
        //assetId
        size += SerializeUtils.sizeOfInt16();
        size += fromNonce.length;
        size += nonce.length;
        size += nextNonce.length;
        size += SerializeUtils.sizeOfBigInteger();
        return size;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public byte[] getFromNonce() {
        return fromNonce;
    }

    public void setFromNonce(byte[] fromNonce) {
        this.fromNonce = fromNonce;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public byte[] getNextNonce() {
        return nextNonce;
    }

    public void setNextNonce(byte[] nextNonce) {
        this.nextNonce = nextNonce;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }
}
