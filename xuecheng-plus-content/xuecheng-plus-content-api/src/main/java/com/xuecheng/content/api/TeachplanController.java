package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 课程计划管理相关的接口
 */
@RestController
@Api(value = "课程计划接口", tags = "课程计划接口")
@RequestMapping("/teachplan")
public class TeachplanController {

    @Resource
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path", example = "1")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> TeachplanController(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping()
    public void saveTeachplan(@RequestBody @Validated SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划删除")
    @ApiImplicitParam(value = "treeplanId", name = "课程计划Id", required = true, dataType = "Long", paramType = "path", example = "1")
    @DeleteMapping("/{treeplanId}")
    public void deleteTeachplan(@PathVariable Long treeplanId){
        teachplanService.deleteTeachplan(treeplanId);
    }

    @ApiOperation("课程计划移动")
    @PostMapping("/{direction}/{treeplanId}")
    public void moveTeachplan(@PathVariable String direction, @PathVariable Long treeplanId){
        teachplanService.moveTeachplan(direction, treeplanId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("解除课程计划和媒资信息绑定")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "treeplanId", name = "课程计划Id", required = true, dataType = "Long", paramType = "path", example = "1"),
        @ApiImplicitParam(value = "mediaId", name = "媒资Id", required = true, dataType = "String", paramType = "path", example = "abc")
    })
    @DeleteMapping("/association/media/{treeplanId}/{mediaId}")
    public void deleteAssociationMedia(@PathVariable Long treeplanId, @PathVariable String mediaId){
        teachplanService.deleteAssociationMedia(treeplanId, mediaId);
    }
}
