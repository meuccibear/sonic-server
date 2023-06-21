package org.cloud.sonic.controller.models.interfaces;

public enum ActionType {
        /**
         * 登陆
         */
        LOGIN("login"),
        /**
         * 进入直播间
         */
        LIVEENTER("liveenter"),
        /**
         * 直播间发言
         */
        LIVECHAT("livechat"),

        /**
         * 直播间点赞
         */
        LIVELIKE("livelike"),

        /**
         * 离开直播间
         */
        LIVELEAVE("liveleave"),

        /**
         * 直播间关注
         */
        LIVEFOLLOW("livefollow");

        private String value;

        ActionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static void main(String[] args) {
                System.out.println(ActionType.LIVECHAT.getValue());
                System.out.println(ActionType.valueOf("livechat".toUpperCase()).getValue());
        }
    }
