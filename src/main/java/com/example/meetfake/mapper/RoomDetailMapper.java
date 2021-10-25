package com.example.meetfake.mapper;

import com.example.meetfake.model.RoomDetail;
import com.example.meetfake.model.RoomDetailExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface RoomDetailMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    long countByExample(RoomDetailExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int deleteByExample(RoomDetailExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int insert(RoomDetail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int insertSelective(RoomDetail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    List<RoomDetail> selectByExample(RoomDetailExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    RoomDetail selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int updateByExampleSelective(@Param("record") RoomDetail record, @Param("example") RoomDetailExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int updateByExample(@Param("record") RoomDetail record, @Param("example") RoomDetailExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int updateByPrimaryKeySelective(RoomDetail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_room_detail
     *
     * @mbg.generated Sun Oct 24 18:59:45 ICT 2021
     */
    int updateByPrimaryKey(RoomDetail record);
}