
package onlinefix;

import models.mysql.word.WordBase;
import play.jobs.Job;

import com.ciaosir.commons.ClientException;

public class UpdateAllWordBase extends Job {

    @Override
    public void doJob() {
        try {
            WordBase.update(0, 128);
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
