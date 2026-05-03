package com.percent99.OutSpecs.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IsParticipant {
  String chatRoomIdArg() default "chatRoomId";
  String userIdArg() default "userId";
}
