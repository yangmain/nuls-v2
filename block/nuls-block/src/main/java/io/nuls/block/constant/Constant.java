/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.constant;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.tools.protocol.ProtocolConfigJson;
import io.nuls.block.model.Node;

import java.util.Comparator;

/**
 * 常量
 *
 * @author captain
 * @version 1.0
 * @date 19-1-22 下午3:34
 */
public interface Constant {

    /**
     * 协议配置文件名称
     * Protocol configuration file name.
     */
    String PROTOCOL_CONFIG_FILE = "protocol-config.json";

    /**
     * 存储每条链的配置信息
     */
    String CHAIN_PARAMETERS = "ChainParameters";
    /**
     * 存储每条链的协议配置信息
     */
    String PROTOCOL_CONFIG = "ProtocolConfig";
    /**
     * 存储每条链的最新高度
     */
    String CHAIN_LATEST_HEIGHT = "ChainLatestHeight";
    /**
     * 存储区块头数据
     */
    String BLOCK_HEADER = "BlockHeader";
    /**
     * 存储区块头高度与hash的键值对
     */
    String BLOCK_HEADER_INDEX = "BlockHeaderIndex";
    /**
     * 分叉链、孤儿链区块数据库前缀
     */
    String CACHED_BLOCK = "CachedBlock";

    /**
     * 共识工作状态
     */
    int CONSENSUS_WORKING = 1;
    /**
     * 共识等待状态
     */
    int CONSENSUS_WAITING = 0;

    /**
     * 区块排序器
     */
    Comparator<Block> BLOCK_COMPARATOR = (o1, o2) -> (int) (o1.getHeader().getHeight() - o2.getHeader().getHeight());

    /**
     * 区块头排序器
     */
    Comparator<BlockHeader> BLOCK_HEADER_COMPARATOR = Comparator.comparingLong(BlockHeader::getHeight);

    /**
     * 协议配置信息排序器
     */
    Comparator<ProtocolConfigJson> PROTOCOL_CONFIG_COMPARATOR = Comparator.comparingInt(ProtocolConfigJson::getVersion);

    /**
     * 节点比较器,默认按信用值排序
     */
    Comparator<Node> NODE_COMPARATOR = Comparator.comparingInt(Node::getCredit).reversed();

    /**
     * 获取网络时间间隔
     */
    long GETTIME_INTERVAL = 30000L;
}
