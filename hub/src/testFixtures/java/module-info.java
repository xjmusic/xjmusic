module services.hub.testFixtures {
  requires java.sql;
  requires org.jooq.codegen;
  requires org.slf4j;
  requires spring.beans;
  requires spring.context;
  requires services.hub.main;
  requires services.lib.main;

  exports io.xj.hub;
}
