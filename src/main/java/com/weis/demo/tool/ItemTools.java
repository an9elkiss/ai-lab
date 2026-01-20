package com.weis.demo.tool;

import cn.hutool.json.JSONUtil;
import com.weis.demo.dto.ItemDTO;
import com.weis.demo.dto.command.ItemSearchCmd;
import com.weis.demo.service.ItemService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemTools {

    private final ItemService itemService;

    @Tool(name = "item-search-tool", value = """
            帮助用户找到合适的商品，返回匹配的商品列表。
            调用此工具后将意图设置为item_search，并将返回的数据填入\\"searchResult\\"字段。
            完全信任item-search-tool返回的数据，即使返回了空列表后者看似无关的商品。
            如果返回了空列表，代表没有合适的商品。根据这一事实结合你的角色定位生成合理的回复。
            
            **注意**Tool使用规范：
            只有当你判断当前用户的意图为item_search时，才允许使用item-search-tool工具。
            """)
    public List<ItemDTO> search(@P("""
            JSON格式的搜索参数，形如：
            {
              "keyWord": "主要品类关键词，如'连衣裙'",
              "scene": "场景",
              "gender": "性别（male/female）",
              "style": "风格",
              "color": "颜色",
              "priceRange": "价格区间"
            }
            尽可能从用户输入中提取并填充上述字段。
            """) String params) {

            ItemSearchCmd cmd = JSONUtil.toBean(params, ItemSearchCmd.class);
            return itemService.search(cmd.getKeyWord());
    }

}
