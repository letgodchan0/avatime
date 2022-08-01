insert into sido values ('0', '서울특별시');
insert into sido values ('0', '부산광역시');
insert into sido values ('0', '대구광역시');
insert into sido values ('0', '인천광역시');
insert into sido values ('0', '광주광역시');
insert into sido values ('0', '대전광역시');
insert into sido values ('0', '울산광역시');
insert into sido values ('0', '세종특별자치시');
insert into sido values ('0', '경기도');
insert into sido values ('0', '강원도');
insert into sido values ('0', '충청도');
insert into sido values ('0', '전라도');
insert into sido values ('0', '경상도');
insert into sido values ('0', '제주도');
insert into age values ('0', '20대');
insert into age values ('0', '30대');
insert into age values ('0', '40대');
insert into age values ('0', '50대 이상');
insert into waiting_room values ('0', '1', now(), '6', '파이썬 초보만', '1', '0');
insert into waiting_room values ('0', '1', now(), '6', '오점뭐먹', '2', '0');
insert into waiting_room values ('0', '2', now(), '6', 'Nullpointerexception', '3', '1');
insert into waiting_room values ('0', '4', now(), '6', '이건 4번째 방', '2', '0');
insert into waiting_room values ('0', '3', now(), '6', '집가고싶다', '2', '1');
insert into waiting_room values ('0', '2', now(), '6', 'ⓕⓤⓒⓚ', '2', '1');
insert into waiting_room values ('0', '2', now(), '6', '울산광역시', '2', '0');
insert into waiting_room values ('0', '3', now(), '6', '화났어?', '2', '0');

insert into avatar (image_path, name) values ("호랑이.png", "호랑이");
insert into avatar (image_path, name) values ("사자.png", "사자");
insert into avatar (image_path, name) values ("강아지.png", "강아지");
insert into avatar (image_path, name) values ("고양이.png", "고양이");
insert into avatar (image_path, name) values ("기린.png", "기린");

insert into user (description, gender, name, profile_image_path, social_id, social_type) values ("testing3", "F", "채윤선", "ts.png", "ssafy3@naver.com", 1);
insert into user (description, gender, name, profile_image_path, social_id, social_type) values ("testing", "M", "김싸피", "sf.png", "ssafy@naver.com", 1);
insert into user (description, gender, name, profile_image_path, social_id, social_type) values ("testing2", "M", "이삼성", "ss.png", "ssafy2@naver.com", 1);
insert into user(social_id, social_type, name, profile_image_path, description, gender) values ("abcxj123@naver.com", 1, "정건우", "https://jira.ssafy.com/secure/useravatar?avatarId=10341", "안녕하세요", "M");

insert into chatting_room (room_id, type) values (1, 2);
insert into chatting_room (room_id, type) values (1, 0);
insert into chatting_room (room_id, type) values (1, 1);
insert into chatting_room (room_id, type) values (1, 1);