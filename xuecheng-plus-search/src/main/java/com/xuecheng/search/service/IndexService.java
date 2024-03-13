package com.xuecheng.search.service;

/**
 * @description 课程索引service
 */
public interface IndexService {

    /**
     * @param indexName 索引名称
     * @param id        主键
     * @param object    索引对象
     * @return Boolean true表示成功,false失败
     * @description 添加索引
     */
    public Boolean addCourseIndex(String indexName, String id, Object object);


    /**
     * @param indexName 索引名称
     * @param id        主键
     * @param object    索引对象
     * @return Boolean true表示成功,false失败
     * @description 更新索引
     */
    public Boolean updateCourseIndex(String indexName, String id, Object object);

    /**
     * @param indexName 索引名称
     * @param id        主键
     * @return java.lang.Boolean
     * @description 删除索引
     */
    public Boolean deleteCourseIndex(String indexName, String id);

}
