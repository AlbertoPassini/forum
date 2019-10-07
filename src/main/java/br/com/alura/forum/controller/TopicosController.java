package br.com.alura.forum.controller;

import br.com.alura.forum.controller.dto.DetalhesTopicoDto;
import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.controller.form.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.model.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

//@Controller
@RestController //When using RestController instead of Controller, @ResponseBody is not necessary
@RequestMapping(value = "/topicos") //When using RequestMapping here, the same URL is applied to all the methods of this controller
public class TopicosController {

	@Autowired //Injects the dependence in this class
	private TopicoRepository topicoRepository;

	@Autowired //Injects the dependence in this class
	private CursoRepository cursoRepository;

	//@RequestMapping(value = "/topicos", method = RequestMethod.GET)
	//@ResponseBody //when using default Spring Controller annotation, this annotation binds the method return value to the response body
	@GetMapping
	@Cacheable(value = "listaDeTopicos")
	public Page<TopicoDto> lista(@RequestParam(required = false) String nomeCurso,
								 @PageableDefault(sort = "id", direction =  Sort.Direction.DESC, page = 0, size = 10) Pageable paginacao) {
		if(nomeCurso==null){
			Page<Topico> topicos = topicoRepository.findAll(paginacao); //The same as 'select * from Topico'
			return TopicoDto.converter(topicos);
		}
		else {
			Page<Topico> topicos = topicoRepository.findByCurso_Nome(nomeCurso, paginacao); //This makes a query by the attribute 'Nome' of the entity 'Curso' that is related to the class 'Topico'
			return TopicoDto.converter(topicos);
		}
	}

	/**
	 * @ResponseEntity - is choose the kind of request used to delivers 201 code -> The request went's ok and a new resource was created with success at the server.
	 *      Obs: It receives a generic that corresponds to the object that will be delivered in the body of the response
	 * @RequestBody - is used for the TopicoForm to get its parameters from the body of the request
	 * URIComponentsBuilder - is used to create an object URI
	 * @Valid - is used to inform Spring that when it injects the TopicoForm, it must validate it (according to the annotations made on TopicoForm attributes)
	 *      Obs: If something isn't according to the validations, the scope of 'cadastrar' won't be executed and the BadRequest code (400) is thrown
	 */
	@PostMapping
	@Transactional
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder){
		Topico topico = form.converter(cursoRepository);
		topicoRepository.save(topico); //Save the object
		URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
		return ResponseEntity.created(uri).body(new TopicoDto(topico)); //Returns the 201 code to the client, the http header with the URL of the new resource created (location) and the representation of this resource
	}

	@GetMapping("/{id}")
	@Transactional
	public ResponseEntity<DetalhesTopicoDto> detalhar(@PathVariable Long id){
		Optional<Topico> optionalTopico = topicoRepository.findById(id); //Selects by id
		if(optionalTopico.isPresent()) {
			return ResponseEntity.ok(new DetalhesTopicoDto(optionalTopico.get()));
		}
		return ResponseEntity.notFound().build();
	}

	@PutMapping("/{id}")
	@Transactional //This annotation informs Spring to commit the transaction
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form){
		Optional<Topico> optionalTopico = topicoRepository.findById(id); //Selects by id
		if(optionalTopico.isPresent()) {
			Topico topico = form.atualizar(id, topicoRepository);
			return ResponseEntity.ok(new TopicoDto(topico));
		}
		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	@Transactional
	@CacheEvict(value = "listaDeTopicos", allEntries = true)
	public ResponseEntity<?> deletar(@PathVariable Long id){
		Optional<Topico> optionalTopico = topicoRepository.findById(id); //Selects by id
		if(optionalTopico.isPresent()) {
			topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.notFound().build();
	}
}
