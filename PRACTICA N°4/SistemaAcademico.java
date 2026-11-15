import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class Persona {
    private final String nombre;
    private final String ci;

    protected Persona(String nombre, String ci) {
        this.nombre = nombre;
        this.ci = ci;
    }

    public String getNombre() { return nombre; }
    public String getCi() { return ci; }
    
    @Override
    public abstract String toString();
}

class Estudiante extends Persona {
    private final String codigo;
    private final List<Inscripcion> inscripciones = new ArrayList<>();

    public Estudiante(String nombre, String ci, String codigo) {
        super(nombre, ci);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }

    public Inscripcion inscribirse(Materia materia) {
        for(Inscripcion i : inscripciones) {
            if(i.getMateria().equals(materia)) {
                throw new IllegalStateException("El estudiante ya está inscrito en esta materia.");
            }
        }
        Inscripcion inscripcion = new Inscripcion(this, materia, LocalDate.now());
        inscripciones.add(inscripcion);
        materia.registrarInscripcion(inscripcion);
        return inscripcion;
    }

    public List<Inscripcion> getInscripciones() {
        return Collections.unmodifiableList(inscripciones);
    }

    @Override
    public String toString() {
        return "Estudiante " + getNombre() + " (código " + codigo + ")";
    }
}

class Ayudante extends Estudiante {
    private Docente docenteAsignado;

    public Ayudante(String nombre, String ci, String codigo, Docente docente) {
        super(nombre, ci, codigo);
        this.docenteAsignado = docente;
    }

    public Docente getDocenteAsignado() { return docenteAsignado; }
    
    public void setDocenteAsignado(Docente docente) {
        this.docenteAsignado = docente;
    }

    @Override
    public String toString() {
        return "Ayudante " + getNombre() + " colaborando con Docente " + docenteAsignado.getNombre();
    }
}

class Docente extends Persona {
    private final String especialidad;
    private final List<Materia> materias = new ArrayList<>();

    public Docente(String nombre, String ci, String especialidad) {
        super(nombre, ci);
        this.especialidad = especialidad;
    }

    public void dictar(Materia materia) {
        materia.setDocente(this);
        if(!materias.contains(materia)) {
            materias.add(materia);
        }
    }

    public Acta generarActa(Materia materia) {
        return new Acta(materia);
    }

    public List<Materia> getMaterias() {
        return Collections.unmodifiableList(materias);
    }

    @Override
    public String toString() {
        return "Docente " + getNombre() + " (" + especialidad + ")";
    }
}

class Universidad {
    private final String nombre;
    private final List<Carrera> carreras = new ArrayList<>();

    public Universidad(String nombre) {
        this.nombre = nombre;
    }

    public Carrera crearCarrera(String nombreCarrera) {
        Carrera c = new Carrera(nombreCarrera);
        carreras.add(c);
        return c;
    }

    public String getNombre() { return nombre; }
    public List<Carrera> getCarreras() { return Collections.unmodifiableList(carreras); }
}

class Carrera {
    private final String nombre;
    private final List<Docente> docentes = new ArrayList<>();

    Carrera(String nombre) {
        this.nombre = nombre;
    }

    public void agregarDocente(Docente docente) {
        if(docentes.contains(docente)) {
            throw new IllegalStateException("Docente ya existe en esta carrera.");
        }
        docentes.add(docente);
    }

    public String getNombre() { return nombre; }
    public List<Docente> getDocentes() { return Collections.unmodifiableList(docentes); }
}

class Materia {
    private final String codigo;
    private final int creditos;
    private Docente docente;
    private final List<Horario> horarios = new ArrayList<>();
    private final List<Inscripcion> inscripciones = new ArrayList<>();

    public Materia(String codigo, int creditos) {
        this.codigo = codigo;
        this.creditos = creditos;
    }

    public Horario agregarHorario(LocalDate fecha, String aula) {
        Horario s = new Horario(fecha, aula);
        horarios.add(s);
        return s;
    }

    void registrarInscripcion(Inscripcion inscripcion) {
        if(!inscripciones.contains(inscripcion)) {
            inscripciones.add(inscripcion);
        }
    }

    void setDocente(Docente docente) {
        if (this.docente != null && this.docente != docente) {
            throw new IllegalStateException("La materia ya tiene un docente asignado.");
        }
        this.docente = docente;
    }

    public String getCodigo() { return codigo; }
    public int getCreditos() { return creditos; }
    public Docente getDocente() { return docente; }
    public List<Horario> getHorarios() { return Collections.unmodifiableList(horarios); }
    public List<Inscripcion> getInscripciones() { return Collections.unmodifiableList(inscripciones); }

    public class Horario {
        private final LocalDate fecha;
        private final String aula;

        private Horario(LocalDate fecha, String aula) {
            this.fecha = fecha;
            this.aula = aula;
        }
        public LocalDate getFecha() { return fecha; }
        public String getAula() { return aula; }
    }
}

interface Calificable {
    double calcularNota();
}

class Inscripcion implements Calificable {
    private final Estudiante estudiante;
    private final Materia materia;
    private final LocalDate fecha;
    private Double nota; 

    Inscripcion(Estudiante estudiante, Materia materia, LocalDate fecha) {
        this.estudiante = estudiante;
        this.materia = materia;
        this.fecha = fecha;
        this.nota = null;
    }

    public void registrarNota(double nota) {
        if (nota < 0 || nota > 100) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 100");
        }
        this.nota = nota;
    }

    @Override
    public double calcularNota() {
        if (this.nota == null) {
            throw new IllegalStateException("La nota aún no ha sido registrada.");
        }
        return nota;
    }

    public Estudiante getEstudiante() { return estudiante; }
    public Materia getMateria() { return materia; }
    public LocalDate getFecha() { return fecha; }
}

class Acta {
    private final LocalDate fecha = LocalDate.now();
    private final String contenido;

    Acta(Materia materia) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACTA ").append(materia.getCodigo()).append(" ").append(fecha).append('\n');
        for (Inscripcion i : materia.getInscripciones()) {
            sb.append(" ").append(i.getEstudiante().getNombre()).append(":")
              .append(i.calcularNota()).append('\n');
        }
        this.contenido = sb.toString();
    }

    @Override
    public String toString() {
        return contenido;
    }
}

public class SistemaAcademico {
    public static void main(String[] args) {
        Universidad umsa = new Universidad("UMSA");
        Carrera informatica = umsa.crearCarrera("Informática");
        
        Docente docente = new Docente("Ana Alba", "1234567", "Ingeniería de Software");
        informatica.agregarDocente(docente);
        
        Materia poo = new Materia("INF-121", 6);
        docente.dictar(poo);
        
        poo.agregarHorario(LocalDate.of(2026, 7, 2), "Aula 101");
        poo.agregarHorario(LocalDate.of(2026, 7, 9), "Aula 101");
        
        Estudiante e1 = new Estudiante("Carlos Flores", "7654321", "E-001");
        Estudiante e2 = new Estudiante("Maria Salas", "7654322", "E-002");
        
        Inscripcion i1 = e1.inscribirse(poo);
        Inscripcion i2 = e2.inscribirse(poo);
        i1.registrarNota(85);
        i2.registrarNota(92);
        
        Ayudante a1 = new Ayudante("Luis Perez", "8888888", "A-001", docente);
        
        List<Persona> personas = Arrays.asList(docente, e1, e2, a1);
        personas.forEach(System.out::println);
        
        Calificable c = i1;
        System.out.println("Nota de " + e1.getNombre() + ": " + c.calcularNota());
        
        Acta acta = docente.generarActa(poo);
        System.out.println(acta);
    }
}
