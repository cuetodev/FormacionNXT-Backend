package com.cuetodev.ej3_1.Persona.infrastructure.controller;

import com.cuetodev.ej3_1.Estudiante.domain.Estudiante;
import com.cuetodev.ej3_1.Feign.IFeignServer;
import com.cuetodev.ej3_1.Persona.infrastructure.controller.dto.output.PersonaFullOutPutDTO;
import com.cuetodev.ej3_1.Persona.infrastructure.repository.port.PersonaRepositoryPort;
import com.cuetodev.ej3_1.Profesor.domain.Profesor;
import com.cuetodev.ej3_1.Profesor.infrastructure.controller.dto.output.ProfesorOutputDTO;
import com.cuetodev.ej3_1.ErrorHandling.NotFoundException;
import com.cuetodev.ej3_1.ErrorHandling.UnprocesableException;
import com.cuetodev.ej3_1.Persona.application.port.PersonaPort;
import com.cuetodev.ej3_1.Persona.domain.Persona;
import com.cuetodev.ej3_1.Persona.domain.PersonaList;
import com.cuetodev.ej3_1.Persona.infrastructure.controller.dto.input.PersonaInputDTO;
import com.cuetodev.ej3_1.Persona.infrastructure.controller.dto.output.PersonaOutputDTO;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
// Con esta linea permito peticiones a través de JavaScript desde todos los dominios (CORS)
@CrossOrigin(origins = "*", methods = RequestMethod.POST)
@RequestMapping
public class PersonaController {

    @Autowired
    private PersonaPort personaPort;

    /*
    --------------
        INSERT
    --------------
    */

    @PostMapping("/addPerson")
    public ResponseEntity<?> insertPersona(@RequestBody PersonaInputDTO personaInputDTO) {
        // Cambio mi InputDTO a la entidad Persona
        Persona persona = personaInputDTO.convertInputDtoToEntity();
        Persona personaRecibida;

        try {
            personaRecibida = personaPort.insertPerson(persona);
        } catch (Exception e) {
            throw new UnprocesableException("Validación de campos errónea");
        }

        // Llamo al insert que tengo en la clase del caso de uso del application
        // este método accederá al del repositorio que se encargará de realizar la consulta
        return new ResponseEntity<>(personaRecibida, HttpStatus.OK);
    }

    /*
    ------------------
        GET / FIND
    ------------------
    */

    @GetMapping("/{id}")
    public ResponseEntity<?> findPersonaById(@PathVariable int id, @RequestParam(defaultValue = "simple", name = "outPutType") String outPutType) throws Exception {

        if (outPutType.equals("simple")) {
            try {
                PersonaOutputDTO personaOutputDTO = new PersonaOutputDTO(personaPort.findPersonaById(id));
                return new ResponseEntity<>(personaOutputDTO, HttpStatus.OK);
            } catch (Exception e) {
                throw new NotFoundException("Persona con ID: " + id + ", no encontrada.");
            }
        } else if (outPutType.equals("full")) {
            try {
                PersonaFullOutPutDTO personaFullOutPutDTO = new PersonaFullOutPutDTO(personaPort.findPersonaById(id));

                Optional<Estudiante> estudiante = personaPort.findEstudianteByPersonaID(id);
                estudiante.ifPresent(personaFullOutPutDTO::setEstudiante);

                Optional<Profesor> profesor = personaPort.findProfesorByPersonaID(id);
                profesor.ifPresent(personaFullOutPutDTO::setProfesor);

                return new ResponseEntity<>(personaFullOutPutDTO, HttpStatus.OK);
            } catch (NotFoundException e) {
                throw new NotFoundException("Persona con ID: " + id + ", no encontrada.");
            }
        }

        return new ResponseEntity<>("OutType invalid (simple / full)", HttpStatus.BAD_REQUEST);
    }

    /*
        La idea en los siguientes métodos es devolver la lista preparada para una paginación real
        de tal forma que tengo un objeto nuevo que es el que voy a devolver (PersonaList)
        este objeto contiene una lista de personas(PersonaOutPutDTO), además del Nº total
        de elementos de la lista.
    */

    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<?> findPersonaByUsuario(@PathVariable String usuario) throws Exception {
        List<PersonaOutputDTO> peopleOutPutDTO = new ArrayList<>();
        PersonaList personaList;
        try {
            // Recorro la lista de personas para transformarla a la lista de personasOutPutDTO
            personaPort.findPersonaByUsuario(usuario).forEach(persona -> {
                try {
                    peopleOutPutDTO.add(new PersonaOutputDTO(persona));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
            // Creo el objeto después de rellenar la lista para obtener el número de elementos totales
            // desde el constructor del objeto.
            personaList = new PersonaList(peopleOutPutDTO);
        } catch (Exception e) {
            return new ResponseEntity<>("Persona con usuario: " + usuario + ", no encontrada.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(personaList, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPeople() throws Exception {
        List<PersonaOutputDTO> peopleOutPutDTO = new ArrayList<>();
        PersonaList personaList;
        try {
            // Recorro la lista de personas para transformarla a la lista de personasOutPutDTO
            personaPort.getAllPeople().forEach(persona -> {
                try {
                    peopleOutPutDTO.add(new PersonaOutputDTO(persona));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
            // Creo el objeto después de rellenar la lista para obtener el número de elementos totales
            // desde el constructor del objeto.
            personaList = new PersonaList(peopleOutPutDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(personaList, HttpStatus.OK);
    }

    /*
    ------------------------
        CRITERIA BUILDER
    ------------------------
    */

    @GetMapping("/findPeople")
    public ResponseEntity<Page<PersonaOutputDTO>> getData(@RequestParam(required = false) String usuario, @RequestParam(required = false) String name, @RequestParam(required = false) String surname, @RequestParam(required = false) @DateTimeFormat(pattern="dd-MM-yyyy") String created_date, @RequestParam(required = false) String condition, @RequestParam(required = false, defaultValue = "noOrder") String orderBy, @RequestParam() int page, @RequestParam(required = false, defaultValue = "10") int size) {
        HashMap<String, Object> data = new HashMap<>();

        if (usuario != null) data.put("usuario", usuario);
        if (name != null) data.put("name", name);
        if (surname != null) data.put("surname", surname);
        if (condition == null) condition = "greater";
        if (!condition.equals("greater") && !condition.equals("less") && !condition.equals("equal")) condition = "greater";
        if (created_date != null) {
            data.put("created_date", created_date);
            data.put("condition", condition);
        }
        if (!orderBy.equals("noOrder")) {
            if (orderBy.equals("user")) orderBy = "user";
            else if (orderBy.equals("name")) orderBy = "name";
        }

        List<Persona> peopleList = personaPort.getData(data, orderBy);
        List<PersonaOutputDTO> peopleOutPutDTOList = new ArrayList<>();

        peopleList.forEach(person -> {
            try {
                peopleOutPutDTOList.add(new PersonaOutputDTO(person));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = (int) (Math.min((start + pageable.getPageSize()), peopleOutPutDTOList.size()));
        Page<PersonaOutputDTO> pageOutPut
                = new PageImpl<PersonaOutputDTO>(peopleOutPutDTOList.subList(start, end), pageable, peopleOutPutDTOList.size());


        return new ResponseEntity<>(pageOutPut, HttpStatus.OK);
    }

    /*
    --------------
        DELETE
    --------------
    */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePersonById(@PathVariable int id) throws Exception {
        int numberOfResults = -1;

        try {
            numberOfResults = personaPort.deletePerson(id);
        } catch (Exception e) {
            return new ResponseEntity<>("No se ha podido borrar a la persona", HttpStatus.BAD_REQUEST);
        }

        if (numberOfResults != 1) {
            throw new NotFoundException("Persona con ID: " + id + ", no encontrada.");
        }

        return new ResponseEntity<>("Persona borrada correctamente", HttpStatus.OK);
    }

    /*
    --------------
        UPDATE
    --------------
    */

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePersonById(@PathVariable int id, @RequestBody PersonaInputDTO personaInputDTO) throws Exception {
        try {
            Persona personaOriginal = personaPort.findPersonaById(id);
            personaPort.updatePerson(personaOriginal, personaInputDTO);
        } catch (Exception e) {
            throw new UnprocesableException("Validación de campos errónea");
        }

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    /*
    --------------------
        RestTemplate
    --------------------
    */

    @GetMapping("profesorRestTemplate/{id}")
    ResponseEntity<?> getProfesor(@PathVariable String id) {
        ResponseEntity<ProfesorOutputDTO> responseEntity = new RestTemplate().getForEntity("http://localhost:8081/profesor/" + id, ProfesorOutputDTO.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        }

        return new ResponseEntity<>("Petición inválida", HttpStatus.BAD_REQUEST);
    }

    /*
    -------------
        Feign
    -------------
    */

    @Autowired
    IFeignServer iFeignServer;

    @GetMapping("profesorFeign/{id}")
    ResponseEntity<?> getProfesorFeign(@PathVariable String id) {
        ResponseEntity<?> responseEntity = iFeignServer.findProfesorByID(id, "simple");

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        }

        return new ResponseEntity<>("Petición inválida", HttpStatus.BAD_REQUEST);
    }
}
