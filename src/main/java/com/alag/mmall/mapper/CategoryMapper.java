package com.alag.mmall.mapper;

import com.alag.mmall.model.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_category
     *
     * @mbggenerated Mon Jul 01 16:02:53 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_category
     *
     * @mbggenerated Mon Jul 01 16:02:53 CST 2019
     */
    int insert(Category record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_category
     *
     * @mbggenerated Mon Jul 01 16:02:53 CST 2019
     */
    int insertSelective(Category record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_category
     *
     * @mbggenerated Mon Jul 01 16:02:53 CST 2019
     */
    Category selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_category
     *
     * @mbggenerated Mon Jul 01 16:02:53 CST 2019
     */
    int updateByPrimaryKeySelective(Category record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mmall_category
     *
     * @mbggenerated Mon Jul 01 16:02:53 CST 2019
     */
    int updateByPrimaryKey(Category record);

    List<Category> selectCategoryByParentId(Integer parentId);
}