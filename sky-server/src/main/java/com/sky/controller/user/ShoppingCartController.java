package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "用户端购物车接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("加入购物车")
    public Result<String> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.add(shoppingCartDTO);
        return Result.success("加入购物车成功");
    }

    @PostMapping("/sub")
    @ApiOperation("购物车减一")
    public Result<String> sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success("购物车数量已减少");
    }

    @PutMapping("/number")
    @ApiOperation("修改购物车数量")
    public Result<String> updateNumber(@RequestParam Long id, @RequestParam Integer number) {
        shoppingCartService.updateNumber(id, number);
        return Result.success("购物车数量修改成功");
    }

    @GetMapping("/list")
    @ApiOperation("查询购物车")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.list());
    }
}
