package com.ssafy.db.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QMeetingRoom is a Querydsl query type for MeetingRoom
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMeetingRoom extends EntityPathBase<MeetingRoom> {

    private static final long serialVersionUID = 116793524L;

    public static final QMeetingRoom meetingRoom = new QMeetingRoom("meetingRoom");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> mainSessionId = createNumber("mainSessionId", Long.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QMeetingRoom(String variable) {
        super(MeetingRoom.class, forVariable(variable));
    }

    public QMeetingRoom(Path<? extends MeetingRoom> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMeetingRoom(PathMetadata metadata) {
        super(MeetingRoom.class, metadata);
    }

}

