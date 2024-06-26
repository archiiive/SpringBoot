package org.zerock.b01.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.b01.domain.Member;
import org.zerock.b01.dto.BoardDTO;
import org.zerock.b01.dto.BoardListReplyCountDTO;
import org.zerock.b01.dto.PageRequestDTO;
import org.zerock.b01.dto.PageResponseDTO;
import org.zerock.b01.repository.MemberRespository;
import org.zerock.b01.service.BoardService;

import java.util.ArrayList;
import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor//생성자 주입
@RequestMapping("/board")

public class BoardController {

    private final BoardService boardService;// 생성자 주입
    private final MemberRespository memberRespository;

    //list로 겟 요청을 보낸다 list로 접속하면 이 매서드가 호출됨
    @GetMapping("/list")
    //리스트 메서드 이 메서드는 페이지 리퀘스트, 모델 타입의 객체를 파라미터로 받는다
    public void list(PageRequestDTO pageRequestDTO, Model model){

        //보드 서비스의 list(pageRequestDTO)를 호출하고 그 결과를
        //boardDTO타입의 pageresponse변수에 저장한다
        // 이 list메서드는 페이지 네이션된 게시판 목록을 가져온다
//        PageResponseDTO< BoardDTO> responseDTO = boardService.list(pageRequestDTO);
        //오류나 어떤 로그를 띄우는지 확인하기 위한 로그인포

        //p.549
        PageResponseDTO<BoardListReplyCountDTO>responseDTO = boardService.listWithReplyCount(pageRequestDTO);
        log.info(responseDTO);

        //responseDTO라는 이름으로 객체에 접근이 가능하게 설정
        //뷰 페이지에서 페이지네이션된 게시판 목록을 표시하는데 사용됨
        model.addAttribute("responseDTO",responseDTO);

    }
                                    //page 691
    @PreAuthorize("hasRole('USER')") // < - ROLE-USER 와 같은 의미다 특정 권환 사용자만 접근 가능하도록 설정
    @GetMapping("/register") //타임리프의 보드 레지스터를 찾으려고한다
    public void registerGet(){

    }
    //page 691  end
    @PostMapping("/register")
    //@Valid어노테이션으로 검증이 필요한 boardDTO 타입의 객체 검증 결과를 담는
    //bindingResult 타입의 객체 그리고 리다이렉트시 속성을 전달하는 redirectAttributes타입의 객체를 파라미터로 받는다
    /* @Valid 어노테이션은 Spring MVC에서 제공하는 데이터 바인딩 검증 기능이다
    //이 어노테이션을 사용하면 컨트롤러 메서드에서 파라미터로 전달되는 객체의 필드 값들이 해당 필드에
    //설정된 제약 조건들을 만족하는지 자동으로 검증됩니다.
    //만약 제약 조건을 만족하지 못하는 필드가 있다면 그 정보는
    //bindingResult객체에 저장되고 이객체를 통해 에러 정보를 조회할 수 있다.
    //이 기능을 통해 개발자는 클라이언트로부터 전달받은 유효성을 검증할수 있다.

     */
    public String registerPost(
            @Valid
            BoardDTO boardDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes){

        log.info("board Post register");

        //값 검증 이후 확인
        if(bindingResult.hasErrors()){ //검증시 에러 있는 경우
            log.info("has errors");
            redirectAttributes.addFlashAttribute("errors",bindingResult.getAllErrors());
            return  "redirect:/board/register";
        }

        //등록 작업
        log.info(boardDTO);

        Long bno = boardService.register(boardDTO);
        redirectAttributes.addFlashAttribute("result",bno);

        return "redirect:/board/list";

    }

    //p.693- p.709
    @PreAuthorize("isAuthenticated()") //인증된 사용자만 접근 가능.
    @GetMapping({"/read","/modify"}) //조회
    //받아와야 하기 때문에 리퀘스트를 사용한다.
    public void read(Long bno, PageRequestDTO pageRequestDTO, Model model){
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info(boardDTO);
        model.addAttribute("dto",boardDTO);
    }

    //p.693-            //p.714 principal 에 username 이 boardDTO에 작성자와 같은지 확인한다.
    //# == EL 표기법 때문에 사용한다.
    @PreAuthorize("principal.username == #boardDTO.writer") //수정 처리는 게시물 작성자와 로그인한 사용자가 같은 경우...
    @PostMapping("/modify")
    public String modify(PageRequestDTO pageRequestDTO ,
                         @Valid BoardDTO boardDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes){
        log.info("board modify post ! ..."+boardDTO);
        if(bindingResult.hasErrors()){
            log.info("has errors");

            String link = pageRequestDTO.getLink();
            //url창에 겟방식으로 접근하기 위함
            redirectAttributes.addFlashAttribute("errors",bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("bno",boardDTO.getBno());
            return "redirect:/board/modify?"+link;
        }
        boardService.modify(boardDTO);
        redirectAttributes.addFlashAttribute("result","modified");
        redirectAttributes.addAttribute("bno",boardDTO.getBno());
        return "redirect:/board/read";

    }
  //  @GetMapping("/remove")
    //RedirectAttributes 주로 스트링값을 넘길때 사용한다.

//    public String remove(Long bno, RedirectAttributes redirectAttributes){
//        log.info("remove post" + bno);
//
//        return
//    }



    //p.693-
    @PreAuthorize("principal.username == #boardDTO.writer")
    @PostMapping("/remove")
    public String remove(BoardDTO boardDTO, RedirectAttributes redirectAttributes){
        log.info("remove post ,,," + boardDTO.getBno());
        boardService.remove(boardDTO.getBno());
        redirectAttributes.addFlashAttribute("result","removed");
        return "redirect:/board/list";
    }


    //page 691 -end-



}
