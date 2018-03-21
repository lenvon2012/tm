package job.apiget;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

public abstract class ConcurrentExecutor<T, W> implements Callable<Boolean> {

	public static int CONCURRENT_THEASHOD = 4;

	private static final Logger log = LoggerFactory
			.getLogger(ConcurrentExecutor.class);

	ExecutorService threadPool;

	CompletionService<T> pool;

	List<W> ids;

	public ConcurrentExecutor(int poolSize, List<W> ids) {

		this.threadPool = Executors.newFixedThreadPool(poolSize);
		this.pool = new ExecutorCompletionService<T>(threadPool);
		this.ids = ids;

	}

	public abstract Callable<T> prepareApiCall(W w);

	public Boolean call() {
		try {
			if (CommonUtils.isEmpty(ids)) {
				return Boolean.TRUE;
			}

			int totalSize = 0;
			for (W w : ids) {
				pool.submit(prepareApiCall(w));
				totalSize++;
			}

			for (int i = 1; i <= totalSize; i++) {
				pool.take().get();
			}

			return Boolean.TRUE;
		} catch (ExecutionException e) {
			log.warn(e.getMessage(), e);

		} catch (InterruptedException e) {
			log.warn(e.getMessage(), e);

		} finally {
			threadPool.shutdown();
		}
		return Boolean.FALSE;
	}

}
