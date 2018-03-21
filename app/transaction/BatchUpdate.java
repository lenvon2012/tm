package transaction;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.JPABase;

import com.ciaosir.client.CommonUtils;

public abstract class BatchUpdate<T extends JPABase, V> implements
		Callable<Integer> {

	private static final Logger log = LoggerFactory
			.getLogger(BatchUpdate.class);

	public static final String TAG = "BatchSaver";

	public List<T> list;

	public int batchSize;

	public String className;

	public BatchUpdate(List<T> list, int batchSize) {
		this.list = list;
		this.batchSize = batchSize;

		if (!CommonUtils.isEmpty(list)) {
			this.className = list.get(0).getClass().getName();
		}

	}

	public Integer call() {
		int count = 0;
		int maxListIndex = list.size() - 1;

		for (T t : list) {
			doUpdate(t);

			if (++count % batchSize == 0 || count == maxListIndex) {
				log.info("Saved[" + className + "]Num : " + count);
				JPATransactionManager.clearEntities();
			}
		}

		log.info("Total Saved Num:" + count);
		return count;
	}

	public abstract V doUpdate(T t);
}
