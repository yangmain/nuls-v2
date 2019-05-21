package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.parse.HashUtil;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
/**
 * 向其他链获取跨链交易
 * @author tag
 * @date 2019/4/4
 */
public class GetOtherCtxMessage extends BaseMessage {
    /**
     * 被请求链协议跨链交易Hash
     * */
    private byte[] requestHash;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(requestHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
    }

    @Override
    public int size() {
        int size = 0;
        size += HashUtil.HASH_LENGTH;
        return size;
    }

    public byte[] getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(byte[] requestHash) {
        this.requestHash = requestHash;
    }
}
