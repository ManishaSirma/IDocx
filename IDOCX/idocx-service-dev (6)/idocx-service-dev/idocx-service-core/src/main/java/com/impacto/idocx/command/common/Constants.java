package com.impacto.idocx.command.common;

public class Constants {
    public enum RESPONSE_STATUS {
        OK(200), ERROR(500), MOVED(301);
        private final int status;

        RESPONSE_STATUS(int i) {
            this.status = i;
        }

        public int getValue() {
            return this.status;
        }
    }

    public enum RESPONSE_MESSAGE {
        SUCCESS("success"),
        ERROR("error");

        private final String message;

        RESPONSE_MESSAGE(String msg) {
            this.message = msg;
        }

        public String getValue() {
            return this.message;
        }
    }

    public enum WORKSPACE_TYPE {
        AUTOWORKSPACE,
        MANUALWORKSPACE,
        BOTH
    }

    public enum RESOURCE_TYPE {
        FOLDER,
        DOCUMENT,
    }

    public enum RESOURCES_ACTION {
        FAVOURITE,
        ARCHIVE,
        TRASH
    }
    public enum EXTENSIONS {
        JPEG,
        PNG,
        ZIP,
        TXT,
        DOC,
        DOCX,
        TIFF,
        XLSX
    }
}
