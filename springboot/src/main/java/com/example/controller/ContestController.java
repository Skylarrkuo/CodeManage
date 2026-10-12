package com.example.controller;

import com.example.common.Result;
import com.example.dto.ContestRankVO;
import com.example.entity.Contest;
import com.example.service.ContestService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 竞赛管理前端操作接口
 */
@RestController
@RequestMapping("/contest")
public class ContestController {

    @Resource
    private ContestService contestService;

    /** 新增 */
    @PostMapping("/add")
    public Result add(@RequestBody Contest contest) {
        contestService.add(contest);
        // 返回新建竞赛的 ID，方便前端在保存后立即跳转到“题目管理”页面
        return Result.success(contest.getId());
    }

    /** 删除 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        contestService.deleteById(id);
        return Result.success();
    }

    /** 修改 */
    @PutMapping("/update")
    public Result updateById(@RequestBody Contest contest) {
        contestService.updateById(contest);
        return Result.success();
    }

    /** 根据ID查询 */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Contest contest = contestService.selectById(id);
        return Result.success(contest);
    }

    /** 查询所有 */
    @GetMapping("/selectAll")
    public Result selectAll(Contest contest) {
        List<Contest> list = contestService.selectAll(contest);
        return Result.success(list);
    }

    /** 分页查询 */
    @GetMapping("/selectPage")
    public Result selectPage(Contest contest,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Contest> page = contestService.selectPage(contest, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 为竞赛按班级批量添加参赛学生（课堂测验场景）
     */
    @PostMapping("/assignClasses")
    public Result assignClasses(@RequestBody AssignClassesRequest request) {
        contestService.assignClasses(request.getContestId(), request.getClassIds());
        return Result.success();
    }

    /**
     * 某场竞赛的 ACM/ICPC 风格排行榜
     */
    @GetMapping("/rank/{contestId}")
    public Result rank(@PathVariable Integer contestId) {
        ContestRankVO rankVO = contestService.getContestRank(contestId);
        return Result.success(rankVO);
    }

    /**
     * 按班级批量添加参赛学生的请求体
     */
    public static class AssignClassesRequest {
        private Integer contestId;
        private List<Integer> classIds;

        public Integer getContestId() {
            return contestId;
        }

        public void setContestId(Integer contestId) {
            this.contestId = contestId;
        }

        public List<Integer> getClassIds() {
            return classIds;
        }

        public void setClassIds(List<Integer> classIds) {
            this.classIds = classIds;
        }
    }
}

