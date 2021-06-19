package org.greeneyed.tplanner.controller;

import javax.naming.InvalidNameException;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.Data;

@Controller
@Data
@RequestMapping(value = BasicController.TRAVEL_PLANNER_BASIC_PATH)
public class BasicController implements EnvironmentAware {

    public static final String TRAVEL_PLANNER_BASIC_PATH = "/travel-planner";
    private Environment environment;

    @RequestMapping(value = "/test")
    @ResponseBody
    public ResponseEntity<String> test() throws InvalidNameException {
        return ResponseEntity.ok("Yeah");
    }

    @RequestMapping(value = "/")
    public String main() {
        return "index";
    }
}