package com.example.meetfake.model;

public class Room {

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column tbl_room.id
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	private Long id;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column tbl_room.roomId
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	private String roomid;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column tbl_room.host
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	private Long host;

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column tbl_room.id
	 * @return  the value of tbl_room.id
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	public Long getId() {
		return id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column tbl_room.id
	 * @param id  the value for tbl_room.id
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column tbl_room.roomId
	 * @return  the value of tbl_room.roomId
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	public String getRoomid() {
		return roomid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column tbl_room.roomId
	 * @param roomid  the value for tbl_room.roomId
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column tbl_room.host
	 * @return  the value of tbl_room.host
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	public Long getHost() {
		return host;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column tbl_room.host
	 * @param host  the value for tbl_room.host
	 * @mbg.generated  Wed Sep 15 11:26:16 ICT 2021
	 */
	public void setHost(Long host) {
		this.host = host;
	}
}