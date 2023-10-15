package br.com.pehGois.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.pehGois.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class filterTaskAuth extends OncePerRequestFilter{
    @Autowired
    private IUserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

            var servletPath = request.getServletPath();
            if (servletPath.startsWith("/tasks/")) {
                //Pegar autentificação
                var authorization = request.getHeader("Authorization");
                var authEncoded = authorization.substring("Basic".length()).trim();

                byte[] authDecode = Base64.getDecoder().decode(authEncoded);
                System.out.println(authDecode);

                var authString = new String(authDecode);
                String[] credentials = authString.split(":");
                String password = credentials[1];
                String username = credentials[0];

                //Validar Usuário
                var user = this.userRepository.findByUsername(username);
                if (user == null) {
                    response.sendError(401);
        
                }else{
                    //Validar senha
                    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                    if (passwordVerify.verified) {
                        request.setAttribute("idUser", user.getId());
                        filterChain.doFilter(request, response);
                    }else{
                        response.sendError(401);
                    }
                }
            }
            else{
                filterChain.doFilter(request, response);
            }
    }
    
}
