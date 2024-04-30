package org.zerock.b01.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.b01.dto.BoardDTO;
import org.zerock.b01.dto.PageRequestDTO;
import org.zerock.b01.dto.PageResponseDTO;
import org.zerock.b01.service.BoardService;

@Controller
@Log4j2
@RequiredArgsConstructor//생성자 주입
@RequestMapping("/board")

public class BoardController {

    private final BoardService boardService;// 생성자 주입

    @GetMapping("/list")
    public void list(PageRequestDTO pageRequestDTO, Model model){

        PageResponseDTO< BoardDTO> responseDTO = boardService.list(pageRequestDTO);
        log.info(responseDTO);

        model.addAttribute("responseDTO",responseDTO);

    }

    @GetMapping("/register") //타임리프의 보드 레지스터를 찾으려고한다
    public void registerGet(){

    }

    @PostMapping("/register")
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

    @GetMapping({"/read","/modify"}) //조회
    //받아와야 하기 때문에 리퀘스트를 사용한다.
    public void read(Long bno, PageRequestDTO pageRequestDTO, Model model){
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info(boardDTO);
        model.addAttribute("dto",boardDTO);
    }

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
    @PostMapping("/remove")
    public String remove(Long bno, RedirectAttributes redirectAttributes){
        log.info("remove post ,,," + bno);
        boardService.remove(bno);
        redirectAttributes.addFlashAttribute("result","remove");
        return "redirect:/board/list";
    }

}