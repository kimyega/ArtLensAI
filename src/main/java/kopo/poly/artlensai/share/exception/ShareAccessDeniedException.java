package kopo.poly.artlensai.share.exception;

public class ShareAccessDeniedException extends RuntimeException {

    public ShareAccessDeniedException() {

        super("본인의 분석 결과만 공유할 수 있습니다.");
    }
}
