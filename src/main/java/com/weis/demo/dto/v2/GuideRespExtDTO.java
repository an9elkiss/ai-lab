package com.weis.demo.dto.v2;

import com.weis.demo.dto.ItemDTO;
import lombok.Data;

import java.util.List;

@Data
public class GuideRespExtDTO extends GuideRespDTO {

    private List<ItemDTO> items;
}
