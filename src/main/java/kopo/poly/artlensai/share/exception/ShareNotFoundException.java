package kopo.poly.artlensai.share.exception;

public class ShareNotFoundException extends RuntimeException {

    public ShareNotFoundException() {

        super("공유 링크를 찾을 수 없습니다.");
    }
}
