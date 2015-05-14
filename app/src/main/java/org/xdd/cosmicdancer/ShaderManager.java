package org.xdd.cosmicdancer;

import static android.opengl.GLES30.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ShaderManager
{
    private Context mContext;
    private AssetManager mAssets;
    public ShaderManager(Context pContext)
    {
        mContext = pContext;
        mAssets = mContext.getAssets();
    }

    /**
     * Loads Shader Source from a given text file,
     * creates a shader object of type pShaderType,
     * and attempts to compile the shader.
     *
     * returns 0 on error or ID of the created shader if succesful
     */
    public int CreateShader(String pShaderFile, int pShaderType)
    {
        String cParsedShader = "";
        String cShaderInfo = "";
        String cShaderType = pShaderType==GL_VERTEX_SHADER ? "Vertex Shader" : "Fragment Shader";

        final int cShaderId = glCreateShader(pShaderType);
        if(cShaderId==0){
            Log.e("CreateShader", "Unable to create "+cShaderInfo);
            return 0;
        }

        final int[] cShaderIv = new int[1];
        try {
            InputStream cStream = mAssets.open(pShaderFile);
            BufferedReader cReader = new BufferedReader(new InputStreamReader(cStream));
            String cNextLine;
            while((cNextLine=cReader.readLine())!=null) {
                cParsedShader+=cNextLine;
            }
        }
        catch(IOException e) {
            Log.e("CreateShader", e.getMessage());
            return 0;
        }

        //GL
        glShaderSource(cShaderId, cParsedShader);
        glCompileShader(cShaderId);

        glGetShaderiv(cShaderId, GL_COMPILE_STATUS, cShaderIv, 0);
        cShaderInfo = glGetShaderInfoLog(cShaderId);

        if(cShaderIv[0]!=GL_TRUE) {
            Log.e("CreateShader", "Error compiling "+cShaderType);
            Log.e("CreateShader", cShaderInfo);
            return 0;
        }

        return cShaderId;
    }

    public int CreateProgram(int pVertShader, int pFragShader)
    {
        String cProgInfo = "";
        final int[] cProgIv = new int[1];

        final int cProgId = glCreateProgram();
        if(cProgId==0){
            Log.e("CreateProgram", "Unable to create GLSL Program");
        }

        glAttachShader(cProgId, pVertShader);
        glAttachShader(cProgId, pFragShader);
        glLinkProgram(cProgId);

        glGetProgramiv(cProgId, GL_LINK_STATUS, cProgIv, 0);
        cProgInfo = glGetProgramInfoLog(cProgId);

        if(cProgIv[0]!=GL_TRUE) {
            Log.e("CreateProgram", "Error linking program");
            Log.e("CreateProgram", cProgInfo);
            return 0;
        }
        return cProgId;
    }
}
