package com.github.devcyntrix.deathchest.api.audit;

import java.io.Closeable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface AuditManager extends Closeable {

    DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    void audit(AuditItem item);

}
