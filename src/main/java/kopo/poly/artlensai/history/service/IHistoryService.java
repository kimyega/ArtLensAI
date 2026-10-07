package kopo.poly.artlensai.history.service;

import java.util.List;

public interface IHistoryService {

    void deleteHistory(List<Long> ids) throws Exception;
}
