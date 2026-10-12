package com.example.controller;

import com.example.auth.RequireRole;
import com.example.common.Result;
import com.example.entity.Clazz;
import com.example.service.ClazzService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理前端操作接口
 *
 * <p>写操作与分页查询仅管理员可用；教师端在竞赛「按班级批量参赛」时需要读取班级下拉，
 * 因此 selectAll / selectById 对教师开放。</p>
 */
@RestController
@RequestMapping("/clazz")
@RequireRole("ADMIN")
public class ClazzController {

    @Resource
    private ClazzService clazzService;

    /** 新增 */
    @PostMapping("/add")
    public Result add(@RequestBody Clazz clazz) {
        clazzService.add(clazz);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        clazzService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    public Result updateById(@RequestBody Clazz clazz) {
        clazzService.updateById(clazz);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result selectById(@PathVariable Integer id) {
        Clazz clazz = clazzService.selectById(id);
        return Result.success(clazz);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    @RequireRole({"ADMIN", "TEACHER"})
    public Result selectAll(Clazz clazz) {
        List<Clazz> list = clazzService.selectAll(clazz);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    public Result selectPage(Clazz clazz,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Clazz> page = clazzService.selectPage(clazz, pageNum, pageSize);
        return Result.success(page);
    }
}
