



package org.utu.studentcare.applogic;
import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import org.utu.studentcare.db.DBCleaner;
import org.utu.studentcare.db.SQLConnection;
import org.utu.studentcare.db.orm.*;
import org.utu.studentcare.graphicmodeui.*;
import org.utu.studentcare.textmodeui.AppLogic;
import org.utu.studentcare.textmodeui.Menu;
import org.utu.studentcare.textmodeui.MenuSession;
import org.utu.studentcare.graphicmodeui.FXController;
import org.utu.studentcare.graphicmodeui.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Main application with all the business logic.
 */
//Oma ratkaisuni toimii ikäänkuin aiemman ratkaisun päällä, tekstiversio pyörii taustalla.
// Slowmoden voi kytkeä päälle vaihtamalla muuttujan totuusarvo, jonkinlaista toiminnallisuutta on
    //Debuggasin kirjautumista vaihtamalla student-luokasta isAdmin tai isTeacher muuttjien totuusarvoja
    //Mikäli käyttäjä on sekä admin että teacher, aukeaa debuggaus-ikkuna, jossa on kaikki vaihtoehdot
    //Yritin liian pitkään rakentaa omaa ratkaisua jo olemassaolevan päälle, joten tästä tuli aika purkkaviritys, saattepahan ainakin hyvät naurut:D
    //Ratkaisut ovat hyvin naiiveja ja ohjelma toimii vain tiettyyn pisteeseen saakka ja silloinkin vain melko näennäisesti.
public class DBApp implements Runnable {
    /**
     * Database file.
     */
    private final String dbFile;
    public final static ObjectProperty<LoginState> logState = new SimpleObjectProperty<>(LoginState.NotLogged);



    /**
     * Interface for reading interactive user input line by line.
     */
    private final Supplier<String> inputStream;

    /**
     * Closes the JavaFX platform and finally executes the provided lambda.
     */
    private final Consumer<Consumer<Void>> shutdownHook;

    /**
     * Prints debug information about sql queries etc. Let's the user login without id/pw.
     */
    private static final boolean debugMode = false;
    public static boolean isLogged = false;
    /**
     * Slows down sql queries artificially.
     */
    public static boolean slowMode = false;

    /**
     * Models the text mode user interface.
     */
    public static boolean isDebugMode(){

        if (debugMode == true) {
            isLogged = true;
        }
        return debugMode;
    }
    public boolean isLogged(){


        return isLogged;
    }
    public static void setSlowMode(Boolean b) {
        slowMode = b;
    }
    private final AppLogic appLogic = new AppLogic(debugMode, "main",
            // alku/pÃ¤Ã¤valikko
            new Menu("main", "S", s -> c -> c
                    .h1("StudentCare Pro 1.0 by Tietotuunarit Ltd")
                    .p("Kirjautunut sisÃ¤Ã¤n kÃ¤yttÃ¤jÃ¤nÃ¤ " + s.user.wholeName())
                    .p("Roolit: opiskelija" + (s.user.isTeacher  ? " + opettaja" : "") + (s.user.isAdmin ? " + hallinto" : ""))
                    .h3("Toiminnot:")
                    .a("joinCourses", "Liity kurssille")
                    .a_("studentCourses", ss -> s.user.attending(s.connection).isEmpty() ? null : "Opiskelemasi kurssit")
                    .a("teachCourses", s.user.isTeacher ? "Ala opettaa kurssia" : null)
                    .a("gradeCourses", s.user.isTeacher ? "Arvioi kurssisuorituksia" : null)
                    .a_("approveCourses", ss -> !s.user.isAdmin ? null : "Opintorekisteriin kirjaukset" + (CourseGrade.waitingApproval(s.connection).isEmpty() ? "(kaikki ok!)" : " (uutta kirjattavaa!)"))
                    .a("wipeDB", s.user.isAdmin ? "Alusta tietokanta" : null)
                    .a("logout", s.active() ? "Kirjaudu ulos" : null)


            ),
            // opiskelija valitsee kurssin, jolle liittyÃ¤
            new Menu("joinCourses", "S", s -> c -> c
                    .h1("Valitse opiskeltava kurssi vapaista kursseista")
                    .nest(cc -> cc.li(s.user.notAttending(s.connection).stream().map(co -> c.a(l -> l.x("joinCourse").x(co.instanceId), co.wholeNameId(40)))))
                    .a("main", "Takaisin pÃ¤Ã¤valikkoon")
            ),
            // tyhjÃ¤ valikko, sivuvaikutus: liittÃ¤Ã¤ opiskeljan kurssille
            new Menu("joinCourse", "SS", s -> {
                s.user.joinCourse(s.connection, CourseInstance.findI(s.connection, s.param(1)).orElseThrow(() -> new AppLogicException("Kurssia ei lÃ¶ytynyt!")));
                return c -> c
                        .h1("LiitytÃ¤Ã¤n kurssille " + s.param(1))
                        .a("joinCourses", "Takaisin");
            }),
            // opettaja valitsee kurssin, jolle liittyÃ¤ opettamaan
            new Menu("teachCourses", "S", s -> c -> c
                    .h1("Valitse opetettava kurssi vapaista kursseista")
                    .nest(cc -> cc.li(s.user.asTeacher().notTeaching(s.connection).stream().map(co -> c.a(l -> l.x("teachCourse").x(co.instanceId), co.wholeNameId(40)))))
                    .a("main", "Takaisin pÃ¤Ã¤valikkoon")
            ),
            // tyhjÃ¤ valikko, sivuvaikutus: liittÃ¤Ã¤ opettajan kurssille
            new Menu("teachCourse", "SS", s -> {
                s.user.asTeacher().teachCourse(s.connection, CourseInstance.findI(s.connection, s.param(1)).orElseThrow(() -> new AppLogicException("Kurssia ei lÃ¶ytynyt!")));
                return c -> c
                        .p("LiitytÃ¤Ã¤n opettamaan kurssille " + s.param(1))
                        .a("teachCourses", "Takaisin");
            }),
            // opiskelijan valikko, josta valita tarkasteltava kurssi. kursseille pitÃ¤Ã¤ olla liittynyt aiemmin
            new Menu("studentCourses", "S", s -> c -> c
                    .h1("Valitse opiskeltavista kursseistasi")
                    .nest(cc -> cc.li(s.user.attending(s.connection).stream().map(co -> c.a(l -> l.x("studentCourse").x(co.instanceId), co.wholeNameId(40)))))
                    .a("main", "Takaisin pÃ¤Ã¤valikkoon")
            ),
            // opettajan valikko, josta valita tarkasteltava kurssi (arviointia varten). kursseille pitÃ¤Ã¤ olla liittynyt aiemmin
            new Menu("gradeCourses", "S", s -> c -> c
                    .h1("Valitse arvioitavista kursseistasi")
                    .nest(cc -> cc.li(s.user.asTeacher().teaching(s.connection).stream().map(co -> c.a(l -> l.x("gradeCourse").x(co.instanceId), co.wholeNameId(40)))))
                    .a("main", "Takaisin pÃ¤Ã¤valikkoon")
            ),
            // opiskelijan kurssinÃ¤kymÃ¤. harjoitusten rakenne, pistelasku, aiemmat kurssisuoritukset, harjoitusten teko
            new Menu("studentCourse", "SS", s -> {
                Student student = Student.find(s.connection, s.user.id).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                Exercises exs = student.exercises(s.connection, s.param(1));
                List<CourseGrade> grades = CourseGrade.findAll(s.connection, student.id, s.param(1));

                return c -> c
                        .h1("Kurssi " + s.param(1))
                        .h3("Harjoitusten rakenne")
                        .li(exs.specs.getExerciseDecls().stream().map(co -> c.p(co.overview())))
                        .h3("Pistelasku:")
                        .li(exs.specs.getGradingDecls().stream().map(co -> c.p(co.toString())))
                        .h3("Kurssisuoritukset:")
                        .li(grades.stream().map(g -> c.p(s2 -> g.overview(s.connection))))
                        .h3("Toiminnot:")
                        .li(exs.exercises.stream().filter(e -> !e.uploaded()).map(e -> c.a(l -> l.x("studentCourseExercise").x(s.params()).x(e.exerciseId), "Palauta harjoitus " + e.exerciseId)))
                        .br()
                        .a(l -> l.x("partCourse").x(s.param(1)).x(s.user.id), "Poistu kurssilta")
                        .a("studentCourses", "Takaisin");
            }),
            // opiskelijan harjoitustehtÃ¤vÃ¤n nÃ¤kymÃ¤. UI:n rajallisuuden vuoksi tÃ¤ssÃ¤ ei vielÃ¤ syÃ¶tetÃ¤ dataa
            new Menu("studentCourseExercise", "SSS", s -> {
                Student student = Student.find(s.connection, s.user.id).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                Exercises exs = student.exercises(s.connection, s.param(1));
                Exercise e = exs.find(s.param(2)).orElseThrow(() -> new AppLogicException("Harjoitusta ei lÃ¶ytynyt!"));
                return c -> c
                        .h1("Kurssi " + s.param(1))
                        .h3("Harjoituksen rakenne")
                        .p(s2 -> e.verboseView(s.connection))
                        .a(l -> l.x("studentCourseExerciseAnswer").x(s.params()), "Anna tehtÃ¤vÃ¤Ã¤n uusi vastaus")
                        .a(l -> l.x("studentCourse").x(s.param(1)), "Takaisin");
            }),
            // pyytÃ¤Ã¤ dataa kÃ¤yttÃ¤jÃ¤ltÃ¤ vastaukseksi (tarkoitus oli ottaa liitetiedosto talteen, mutta UI on aika rajallinen siihen)
            new Menu("studentCourseExerciseAnswer", "SSS", s -> {
                Student student = Student.find(s.connection, s.user.id).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                Exercises exs = student.exercises(s.connection, s.param(1));
                Exercise e = exs.find(s.param(2)).orElseThrow(() -> new AppLogicException("Harjoitusta ei lÃ¶ytynyt!"));

                s.outputStream.println("Anna uusi vastaus tehtÃ¤vÃ¤Ã¤n " + e.exerciseId + ":");
                String answer = s.inputStream.get();

                s.outputStream.println("Anna myÃ¶s vapaavalintainen kommentti:");
                String comment = s.inputStream.get();

                e.upload(s.connection, answer, comment);

                return c -> c
                        .h1("Kurssi " + s.param(1))
                        .a(l -> l.x("studentCourse").x(s.param(1)), "Takaisin");
            }),
            // poistaa opiskelijan kurssilta
            new Menu("partCourse", "SS", s -> {
                s.user.partCourse(s.connection, CourseInstance.findI(s.connection, s.param(1)).orElseThrow(() -> new AppLogicException("Kurssia ei lÃ¶ytynyt!")));
                return c -> c
                        .h1("Poistutaan kurssilta " + s.param(1))
                        .a("studentCourses", "Takaisin");
            }),
            // opettajalle, kurssin arviointiruutu
            new Menu("gradeCourse", "SS", s -> {
                CourseInstance course = CourseInstance.findI(s.connection, s.param(1)).orElseThrow(() -> new AppLogicException("Kurssia ei lÃ¶ytynyt!"));
                ExerciseSpecs specs = course.exerciseSpecs();
                List<Student> students = course.students(s.connection);
                return c -> c
                        .h1("Kurssi " + s.param(1))
                        .h3("Harjoitusten rakenne:")
                        .li(specs.getExerciseDecls().stream().map(co -> c.p(co.overview())))
                        .h3("Pistelasku:")
                        .li(specs.getGradingDecls().stream().map(co -> c.p(co.toString())))
                        .h3("Suorittajia:")
                        .li(students.stream().map(st -> c.a(l -> l.x("gradeCourseStudent").x(s.params()).x(st.id),
                                c.p(s2 -> st.wholeNameId() + ": " + st.exercises(s.connection, s.param(1)).status()))))
                        .br()
                        .a(l -> l.x("abandonCourse").x(s.params()).x(s.user.id), "Lopeta kurssin opetus")
                        .a("gradeCourses", "Takaisin");
            }),
            // poistaa opettajan kurssin opettajista
            new Menu("abandonCourse", "SS", s -> {
                s.user.asTeacher().abandonCourse(s.connection, CourseInstance.findI(s.connection, s.param(1)).orElseThrow(() -> new AppLogicException("Kurssia ei lÃ¶ytynyt!")));
                return c -> c
                        .h1("Poistutaan kurssilta (tuhoaa kurssi-instanssin) " + s.param(1))
                        .a("gradeCourses", "Takaisin");
            }),
            // tietyn kurssin tietyn opiskelijan arviointi
            new Menu("gradeCourseStudent", "SSI", s -> {
                Student student = Student.find(s.connection, s.paramI(2)).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                Exercises exs = student.exercises(s.connection, s.param(1));
                Optional<CourseGrade> cg = CourseGrade.find(s.connection, student.id, s.param(1));
                Optional<ValRange> grade = exs.grade();
                boolean gradedOrNewExercises = exs.latest().map(latestEx -> cg.map(g -> g.gradeDate.compareTo(latestEx) < 0).orElse(false)).orElse(false);
                int gradeNum = grade.map(g -> g.min == g.max && gradedOrNewExercises ? (int) g.min : -1).orElse(-1);
                return c -> c
                        .h1("Kurssi " + s.param(1))
                        .h2(student.wholeNameId())
                        .h3("Harjoitukset:")
                        .li(exs.exercises.stream().map(ex -> c.a(l -> l.x("gradeCourseStudentExercise").x(s.params()).x(ex.exerciseId), s2 -> c.p(ex.status(s.connection)))))
                        .p(exs.exercises.isEmpty() ? "Ei suorituksia!" : null)
                        .h3("Pistelasku:")
                        .li(exs.specs.getGradingDecls().stream().map(co -> c.p(co.toString())))
                        .h3("Arvosanat:")
                        .p("Pisteiden mukaan: " + grade.map(ValRange::toString).orElse("Kurssilla ei ole laskukaavaa arvosanalle!"))
                        .p("Kirjattu kurssiarvosana: " + cg.map(CourseGrade::shortOverview).orElse("-"))
                        .a(l -> l.x("gradeCourseStudentWith").x(s.params()).x(gradeNum), gradeNum == -1 ? null : "HyvÃ¤ksy kurssiarvosana " + gradeNum)
                        .a(l -> l.x("gradeCourse").x(s.param(1)), "Takaisin");
            }),
            // tietyn kurssin tietyn opiskelijan kurssisuorituksen kirjaus hyvÃ¤ksyttÃ¤vÃ¤ksi opintorekisteriin
            new Menu("gradeCourseStudentWith", "SSII", s -> {
                Student student = Student.find(s.connection, s.paramI(2)).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                CourseGrade cg = new CourseGrade(s.paramI(2), s.param(1), s.paramI(3), s.connection.now(), s.user.id);
                cg.updateDB(s.connection);
                Exercises exs = student.exercises(s.connection, s.param(1));
                return c -> c
                        .h1("Kurssi " + s.param(1))
                        .h2(student.wholeNameId())
                        .h3("Kirjattu kurssisuoritus:")
                        .p(s2 -> cg.overview(s.connection))
                        .h3("Harjoitukset:")
                        .li(exs.exercises.stream().map(ex -> c.a(l -> l.x("gradeCourseStudentExercise").x(s.param(1)).x(s.paramI(2)).x(ex.exerciseId), s2 -> c.p(ex.status(s.connection)))))
                        .h3("Pistelasku:")
                        .li(exs.specs.getGradingDecls().stream().map(co -> c.p(co.toString())))
                        .a(l -> l.x("gradeCourseStudent").x(s.param(1)).x(s.paramI(2)), "Takaisin");
            }),
            // tietyn kurssin tietyn opiskelijan tietyn harjoituksen arviointi pisteillÃ¤
            new Menu("gradeCourseStudentExercise", "SSIS", s -> {
                Student student = Student.find(s.connection, s.paramI(2)).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                Exercises exs = student.exercises(s.connection, s.param(1));
                Exercise e = exs.find(s.param(3)).orElseThrow(() -> new AppLogicException("Harjoitusta ei lÃ¶ytynyt!"));
                ExerciseSpec spec = e.specification.orElseThrow(() -> new AppLogicException("Harjoituksen kuvausta ei lÃ¶ytynyt!"));
                return c -> c
                        .h1(s.param(1))
                        .h2(student.wholeNameId())
                        .p(s2 -> e.verboseView(s.connection))
                        .br()
                        .x(e.graded() ? c.br() : c
                                .p("Arvioi pistein:")
                                .li(spec.possibleValues().stream().map(v -> c.a(l -> l.x("gradeCourseStudentExerciseWith").x(s.params()).x(v), v.toString() + " pistettÃ¤")))
                        )
                        .a(l -> l.x("gradeCourseStudent").x(s.param(1)).x(s.paramI(2)), "Takaisin");
            }),
            // tietyn kurssin tietyn opiskelijan tietyn harjoituksen arvioinnin talletus
            new Menu("gradeCourseStudentExerciseWith", "SSISD", s -> {
                Student student = Student.find(s.connection, s.paramI(2)).orElseThrow(() -> new AppLogicException("Opiskelijaa ei lÃ¶ytynyt!"));
                Exercises exs = student.exercises(s.connection, s.param(1));
                Exercise e = exs.find(s.param(3)).orElseThrow(() -> new AppLogicException("Harjoitusta ei lÃ¶ytynyt!"));
                Exercise e2 = e.grade(s.connection, s.user.id, s.paramD(4), "Hienoa.");
                return c -> c
                        .h1(s.param(1))
                        .h2(student.wholeNameId())
                        .p(s2 -> "Kirjattu suoritus: " + e2.verboseView(s.connection))
                        .a(l -> l.x("gradeCourseStudent").x(s.param(1)).x(s.paramI(2)), "Takaisin");
            }),
            // tietyn kurssin tietyn opiskelijan tietyn suorituksen kirjaus opintorekisteriin
            new Menu("approveCourse", "SISS", s -> {
                CourseGrade cg = CourseGrade.find(s.connection, s.paramI(1), s.param(2), s.param(3)).orElseThrow(() -> new AppLogicException("No such element"));
                CourseInstance ci = cg.course(s.connection);
                Student st = cg.student(s.connection);
                cg.approve(s.connection, s.user.id);
                return c -> c
                        .h1("Kurssisuorituksen kirjaus opintorekisteriin")
                        .p("Kirjattu opintorekisteriin: " + cg.gradeDate + ": " + ci.wholeNameId(30) + " - " + st.wholeNameId() + " - " + cg.adminDate)
                        .a("approveCourses", "Takaisin");
            }),
            // kurssisuoritusten kirjaaminen opintorekisteriin (hallinnon yleisnÃ¤kymÃ¤)
            new Menu("approveCourses", "S", s -> {
                List<CourseGrade> courseGradesWaiting = CourseGrade.waitingApproval(s.connection);
                List<CourseGrade> courseGradesApproved = CourseGrade.lastApproved(s.connection, 10);
                return c -> c
                        .h1("Hallinto")
                        .h3("Viimeksi opintorekisteriin merkitty:")
                        .li(courseGradesApproved.stream().map(cg -> c.p(ss -> cg.adminOverview(s.connection))))
                        .h3("Arvioituja suorituksia jonossa:")
                        .li(courseGradesWaiting.stream().map(cg -> c.a(l -> l.x("approveCourse").x(cg.studentId).x(cg.instanceId).x(cg.gradeDate), s2 -> c.p(cg.adminOverview(s.connection)))))
                        .br()
                        .a("main", "Takaisin pÃ¤Ã¤valikkoon");
            }),
            // alustaa tietokannan
            new Menu("wipeDB", "S", s -> c -> c
                    .p(ss -> { new DBCleaner(s.connection).wipeTables().populateTables(); return "Tietokanta alustettu uudelleen!"; })
                    .a("main", "Takaisin pÃ¤Ã¤valikkoon")

            ),
            // kirjautuu ulos
            new Menu("logout", "S", ss -> {
                ss.close();
                return c -> c.p("Kirjaudutaan ulos..");
            })

    );



    public DBApp(String dbFile, Supplier<String> inputStream, Consumer<Consumer<Void>> shutdownHook) throws SQLException, AppLogicException {
        this.dbFile = dbFile;
        this.inputStream = inputStream;
        this.shutdownHook = shutdownHook;
    }

    /**
     * Loops until a valid username/password pair is provided -> return the authenticated user
     * in case the user provides 'quit' as a name, return empty
     */
    //Hakee login-lomakkeesta käyttäjän antamat tiedot ja kirjautuu sen perusteella
    //Valitsee avattavan ikkunan käyttäjän tietojen perusteella, mikäli käyttäjällä on molemmat sekä admin- että opettajaoikeudet
    //avataan debug-ikkuna, muuten avataan käyttäjän roolin mukainen ikkuna.
    //Kirjautuminen toimii minun ratkaisussani valitettavasti vain kerran, jos kirjautuu ulos ei pääse kirjautumaan uudelleen ennen
    //ohjelman uudelleenkäynnistystä
    public static Optional<Student> login(SQLConnection connection) throws SQLException, AppLogicException {

        Optional<Student> login = Optional.empty();
        String id = "";
        String pw = "";
        while (login.equals(Optional.empty()) && logState.getValue().toString().contains("Not")) {
            if (FXController.getUserID() != null) {
                // System.out.println("Enter your username:");
                id = FXController.getUserID();
                // String id = inputStream.get();
                // System.out.println("TÃ¤ssÃ¤ lukituksen tila " + isLogged);

                if (id.equals("quit")) return login;
            }
            if (FXController.getPassword() != null) {
                // System.out.println("Enter your password:");
                pw = FXController.getPassword();
                //String pw = inputStream.get();
            } else
                try {
                    Thread.currentThread().wait();
                    {

                    }
                } catch (Exception e) {

                }
            login = Student.authenticate(connection, id, pw);


        }
        try {

 //Tarkastetaan mitkä oikeudet käyttäjällä on ja valitaan avattava ikkuna
            if (Student.find(connection, id).get().isTeacher && !Student.find(connection, id).get().isAdmin) {
                logState.setValue(LoginState.TeacherLogged);
                Controller.setAppState(AppState.Teacher);
            }
            if (Student.find(connection, id).get().isAdmin && Student.find(connection, id).get().isTeacher) {

                Controller.setAppState(AppState.Debug);
            }
            if (!Student.find(connection, id).get().isAdmin && !Student.find(connection, id).get().isTeacher) {
                Controller.setAppState(AppState.Student);
                logState.setValue(LoginState.StudenLogged);
            }
            if (Student.find(connection, id).get().isAdmin && !Student.find(connection, id).get().isTeacher) {
                logState.setValue(LoginState.SecretaryLogged);
                Controller.setAppState(AppState.Secretary);
            }

            return login;
        } catch (NoSuchElementException nsee) {
            System.out.println("Ei se menoa haittaa");
        }
        return login;
    }

    /**
     * In case a debug mode is requested, the db must contain a student&teacher&admin person, otherwise
     * nuke the db and try again.
     */
    private Optional<Student> findDebugUserOrNukeTheDBAndThenFindAgain(SQLConnection connection) throws SQLException, AppLogicException {
        Optional<Student> debugUser = Optional.empty();
        if (debugMode) {
            while (!debugUser.isPresent()) {
                try {
                    debugUser = connection.findFirst(Teacher::fromDB, "select * from personnel where isTeacher == true and isAdmin == true");
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
                if (!debugUser.isPresent()) {
                    System.err.println("The database was broken (no admin & teacher entities for debugging). Rebuilding...");
                    DBCleaner.initDB(connection, 5);
                }
            }
        }
        return debugUser;
    }

    /**
     * Text mode UI's main loop:
     * 1) start a sql connection
     * 2) loop until user decides to quit
     * 3) activate shutdown hooks
     */
    public void run() {

        Path path = Paths.get(dbFile);
        String dbPath = path.toString();

        boolean emptyDB = !Files.exists(path);

        // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        try (SQLConnection connection = SQLConnection.createConnection(slowMode ? "slow" + dbPath : dbPath, debugMode)) {
            if (emptyDB) {
                System.err.println("Database " + path + " did not exist. Created a new DB instance.");
                DBCleaner.initDB(connection, 0);
            }


            Optional<Student> debugUser = findDebugUserOrNukeTheDBAndThenFindAgain(connection);
            Optional<Student> user;
            do {
                if (debugMode)
                    System.out.println(new DBCleaner(connection).debug());

                // in debug mode, the first login attempt is done with the debug user
                user = debugUser.isPresent() ? debugUser : login(connection);

                debugUser = Optional.empty();

            } while (appLogic.control(inputStream, new MenuSession(user, connection, System.out, inputStream)));
        } catch (Exception e) {
            handleEx(e, shutdownHook);
            return;
        }

        shutdownHook.accept(s -> {
        });
    }

    /**
     * Exception pretty printer
     *
     * @param ex
     * @param shutdownHook
     */
    static void handleEx(Exception ex, Consumer<Consumer<Void>> shutdownHook) {
        try {
            throw ex;
        } catch (SQLException e) {
            System.err.println("SQL ERROR:" + e.getMessage());
            if (e.getSQLState() != null) System.err.println(e.getSQLState());

            if (e.getMessage().contains("SQLITE_LOCKED")) {
                System.out.println("Lukitusvirhe");
                System.out.println("------------\n");
                System.out.println("Tietokanta on lukittu, ts. tietokanta on juuri nyt kÃ¤ytÃ¶ssÃ¤ joko");
                System.out.println("StudentCaren toisessa instanssissa (sulje stop-napilla IDE:stÃ¤)");
                System.out.println("tai editoit kantaa jollain toisella ohjelmalla. Ohjelma sulkeutuu");
                System.out.println("nyt pakotetusti, ettei tietokanta vahingossa korruptoidu.");
            }

        } catch (Exception e) {
            System.err.println("ERROR:" + e.getMessage());
        }
        System.err.println(" ");
        shutdownHook.accept(s -> {
            for (Object o : ex.getStackTrace()) System.err.println(" -> " + o);
        });
    }

    /**
     * Initializes the text mode UI
     *
     * @param dbFile
     * @param shutdownHook
     * @param commandQueue
     */
    public static void init(String dbFile, Consumer<Consumer<Void>> shutdownHook, LinkedBlockingQueue<String> commandQueue) throws AppLogicException, SQLException {
        //AppLogic teacherLogic = new AppLogic("/org/utu/studentcare/db/teachermenu.fxml");


        // }
        //GraphAppLogic graphAppLogic2 = new GraphAppLogic("Hallinto", adminWindow);

        Supplier<String> inputStream = () -> {
            while (true) {
                try {
                    return commandQueue.take();
                } catch (InterruptedException e) {
                }
                // in case the stdin breaks, avoid making the GUI totally unresponsive
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        };
        new Thread(() -> {
            try {
                new DBApp(dbFile, inputStream, shutdownHook).run();
            } catch (Exception e) {
                handleEx(e, shutdownHook);
            }
        }).start();
    }
}
















