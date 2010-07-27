/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.interceptor.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.interceptor.InvocationContext;

import org.jboss.interceptor.spi.model.InterceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class InterceptionChain
{

   private final Logger log = LoggerFactory.getLogger(InterceptionChain.class);

   private Object target;

   private Method targetMethod;

   private int currentPosition;

   private final InterceptionType interceptionType;

   private List<InterceptorInvocation.InterceptorMethodInvocation> interceptorMethodInvocations;

   public InterceptionChain(Collection<InterceptorInvocation<?>> interceptorInvocations, InterceptionType interceptionType, Object target, Method targetMethod)
   {
      this.interceptionType = interceptionType;
      this.target = target;
      this.targetMethod = targetMethod;
      this.currentPosition = 0;
      interceptorMethodInvocations = new ArrayList<InterceptorInvocation.InterceptorMethodInvocation>();
      for (InterceptorInvocation<?> interceptorInvocation : interceptorInvocations)
      {
         interceptorMethodInvocations.addAll(interceptorInvocation.getInterceptorMethodInvocations());
      }
   }

   public Object invokeNext(InvocationContext invocationContext) throws Throwable
   {

      try
      {
         if (hasNext())
         {
            InterceptorInvocation.InterceptorMethodInvocation nextInterceptorMethodInvocation = interceptorMethodInvocations.get(currentPosition++);
            if (log.isTraceEnabled())
            {
               log.trace("Invoking next interceptor in chain:" + nextInterceptorMethodInvocation.method.toString());
            }
            if (nextInterceptorMethodInvocation.method.getJavaMethod().getParameterTypes().length == 1)
            {
               return nextInterceptorMethodInvocation.invoke(invocationContext);
            }
            else if (nextInterceptorMethodInvocation.method.getJavaMethod().getParameterTypes().length == 0)
            {
               nextInterceptorMethodInvocation.invoke(null);
               while (hasNext())
               {
                  nextInterceptorMethodInvocation = interceptorMethodInvocations.get(currentPosition++);
                  if (nextInterceptorMethodInvocation.method.getJavaMethod().getParameterTypes().length != 0)
                  {
                     throw new IllegalStateException("Impossible state: lifecycle callback interceptor method on target class has more than one argument:" + nextInterceptorMethodInvocation.getMethod());
                  }
                  nextInterceptorMethodInvocation.invoke(null);
               }
               return null;
            }
            else
            {
               throw new IllegalStateException("Impossible state: interceptor method has more than one argument:" + nextInterceptorMethodInvocation.getMethod());
            }
         }
         else
         {
            if (targetMethod != null)
            {

               return targetMethod.invoke(target, invocationContext.getParameters());

            }
            else
            {
               return null;
            }
         }
      }
      catch (InvocationTargetException e)
      {
         throw e.getCause();
      }
   }

   public boolean hasNext()
   {
      return currentPosition < interceptorMethodInvocations.size();
   }

}
